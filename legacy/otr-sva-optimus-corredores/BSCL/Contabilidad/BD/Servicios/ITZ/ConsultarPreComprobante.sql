SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarPreComprobante";
GO
CREATE PROCEDURE 
-- 
--  Proposito: Vista 
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 08/09/2016
--  Id Servicio = 12008
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarPreComprobante" (
	IN p_IdEmpresa   			NVARCHAR(3)   	DEFAULT '',
    IN p_Estado    				NVARCHAR(1)   	DEFAULT '',
    IN p_CodigoUsuario  		NVARCHAR(40)  	DEFAULT '',
    IN p_FechaContableInicial 	NVARCHAR(10)  	DEFAULT '',
    IN p_FechaContableFinal 	NVARCHAR(10)  	DEFAULT '',
    IN p_IdTipoContabilidad 	SMALLINT  		DEFAULT 0,
    IN p_FechaProceso   		NVARCHAR(10)  	DEFAULT '',
    IN p_IdPreComprobante  		DECIMAL(18,0) 	DEFAULT 0,
    IN p_FolioContabilidad 		DECIMAL(18,0) 	DEFAULT 0,
	-- Parametros salida del servicio publico
	OUT p_out_TabPreComprobantes TABLE ( 
	                                    "IdPreComprobante"            BIGINT,
										"NombreEmpresa"               NVARCHAR(60),
										"Ejercicio"                   BIGINT,
										"FechaContable"               NVARCHAR(10),
										"FechaProceso"                NVARCHAR(10),
										"HoraProceso"                 NVARCHAR(8),
										"FolioContabilidad"           BIGINT,
										"CantidadAsientos"            BIGINT,
										"Estado"                      NVARCHAR(1),
										"NombreEstado"				  NVARCHAR(50),
										"Glosa"                       NVARCHAR(150),
										"IdReversa"                   BIGINT,
										"Tipo"                        NVARCHAR(1),
										"CodigoUsuario"               NVARCHAR(40),
										"IdTipoContabilidad"          BIGINT,
										"NombreTipoContabilidad"      NVARCHAR(90),
										"IdEmpresa"                   BIGINT,
										"Debe"                        DECIMAL,
										"Haber"                       DECIMAL,
										"IdMoneda"					  NVARCHAR(3),
										"IdEstadoPrograma"			  NVARCHAR(3),
										"IdPrograma"				  BIGINT
	                                     ),
	-- Sección Fija ----
	--Salidas para mensajes de advertencias y errores
	OUT p_out_ResultadoProceso          table ("Campo" NVARCHAR(40),"Id" BIGINT, "Servicio" NVARCHAR(250), "Resultado" NVARCHAR(40)) DEFAULT EMPTY,
	OUT p_out_AdvertenciasContador      int,
	OUT p_out_AdvertenciasDetalle       table ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,  
	OUT p_out_ErroresContador           int,   
	OUT p_out_ErroresDetalle            table ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
	-- FIN Sección Fija
	)
      LANGUAGE SQLSCRIPT
      SQL SECURITY INVOKER
      DEFAULT SCHEMA "BSCL"
      --READS SQL DATA
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_IdMoneda					NVARCHAR(3);
	DECLARE v_ErroresDetalle			TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520)) DEFAULT
												SELECT 0 AS "IdComprobante", '' AS "Codigo", '' AS "Descripcion" FROM DUMMY WHERE 1=2; 	
    DECLARE v_RechazadosPorValidacion	TABLE("IdComprobante" BIGINT) DEFAULT
    											SELECT 0 AS "IdComprobante" FROM DUMMY WHERE 1=2;

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN		
		v_hash= Select 0 As "IdInterno", '' As "Codigo", '' As "CodigoDato1", '' As "ValorDato1", '' As "CodigoDato2",
                '' As "ValorDato2" From Dummy;	
        v_out_hash = Select 0 As "IdInterno", '' As "Codigo", '' As "Descripcion" From Dummy;

        Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);

		p_out_ErroresDetalle = Select "IdInterno" As "IdComprobante", "Codigo"  as "Codigo",
		                              "Descripcion" As "Descripcion" 
		                       From :v_out_hash Union All
		                       Select * From :p_out_ErroresDetalle; 

		SELECT COUNT(1) INTO p_out_ErroresContador
		FROM :p_out_ErroresDetalle;
	END;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT 0 As "IdComprobante", v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador 
			FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	

	p_out_AdvertenciasContador := 0;
	p_out_ErroresContador	   := 0;
	v_IdMoneda				   :='';
	
	SELECT "Valor" INTO v_IdMoneda 
	FROM "BSCL.Plataforma.BD::PLA.Configuracion"
	WHERE "IdEmpresa" = p_IdEmpresa AND "CodigoParametro" = 'PLA.MonedaSistema';
	

	BEGIN
		p_out_TabPreComprobantes = 
			SELECT CTB_itzPreComprobante."IdPreComprobante" 									AS "IdPreComprobante",
				   IFNULL(CTB_itzEmpresa."Nombre", 'S/N') 										AS "NombreEmpresa",
				   CTB_itzPreComprobante."Ejercicio"											AS "Ejercicio",
				   CAST(CAST(CTB_itzPreComprobante."FechaContable" AS TIMESTAMP) AS CHAR(10)) 	AS "FechaContable",
				   CAST(CAST(CTB_itzPreComprobante."FechaProceso" AS TIMESTAMP) AS CHAR(10)) 	AS "FechaProceso",
				   CTB_itzPreComprobante."HoraProceso" 											AS "HoraProceso",
				   CTB_itzPreComprobante."FolioContabilidad" 									AS "FolioContabilidad",
				   (SELECT COUNT(1) 
				      FROM "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento
				     WHERE CTB_itzPreComprobanteAsiento."IdPreComprobante" = CTB_itzPreComprobante."IdPreComprobante" 
				       AND CTB_itzPreComprobanteAsiento."Estado"  IN ('V','C','A')) 			AS "CantidadAsientos",
				   CTB_itzPreComprobante."Estado"											 	AS "Estado",
				   EstadoContabilidad."NombreEstado"											AS "NombreEstado",
				   CTB_itzPreComprobante."Glosa"												AS "Glosa",
				   CTB_itzPreComprobante."IdReversa"											AS "IdReversa",
				   CTB_itzPreComprobante."Tipo" 												AS "Tipo",
				   CTB_itzPreComprobante."CodigoUsuario" 										AS "CodigoUsuario",
				   CTB_itzPreComprobante."IdTipoContabilidad" 									AS "IdTipoContabilidad",
--				   CTB_itzContabilidadTipo."Nombre" 											AS "NombreTipoContabilidad",
				   CTB_itzContabilidadTipo."Nombre" || ' - ' || TRIM(CAST("IdPreComprobante" AS NVARCHAR(20))) AS "NombreTipoContabilidad", 
				   CTB_itzPreComprobante."IdEmpresa" 											AS "IdEmpresa",
				   (SELECT SUM(CAST("MontoDebe" AS DECIMAL)) 
				      FROM "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento 
				     WHERE CTB_itzPreComprobanteAsiento."IdPreComprobante" = CTB_itzPreComprobante."IdPreComprobante" 
				       AND CTB_itzPreComprobanteAsiento."Estado" IN ('V','C','A')) 				AS "Debe",
				   (SELECT SUM(CAST("MontoHaber" AS DECIMAL)) 
				      FROM "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento
				     WHERE CTB_itzPreComprobanteAsiento."IdPreComprobante" = CTB_itzPreComprobante."IdPreComprobante" 
				       AND CTB_itzPreComprobanteAsiento."Estado" IN ('V','C','A')) 				AS "Haber",
--				       v_IdMoneda																AS "IdMoneda",
				       (	SELECT max("CodigoMoneda")		   
						  	FROM "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento	
						  	INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" AS CTB_itzEventoPreComprobanteAsiento
						  	ON (CTB_itzPreComprobanteAsiento."IdPreComprobanteAsiento" = CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento")
						    LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Evento" AS CTB_itzEvento 
						    ON	CTB_itzEventoPreComprobanteAsiento."IdEvento" = CTB_itzEvento."IdEvento"
							WHERE CTB_itzPreComprobanteAsiento."IdPreComprobante" = CTB_itzPreComprobante."IdPreComprobante" )			AS "IdMoneda",
				   COALESCE(Programa."IdEstado",'')												AS "IdEstadoPrograma",
				   CTB_itzPreComprobante."IdPrograma" AS "IdPrograma"
			  FROM "BSCL.Contabilidad.BD::ITZ.PreComprobante" AS CTB_itzPreComprobante 
				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo" AS CTB_itzContabilidadTipo ON 
					CTB_itzContabilidadTipo."IdTipoContabilidad" = CTB_itzPreComprobante."IdTipoContabilidad" 
			  	LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Empresa" AS CTB_itzEmpresa ON 
					CTB_itzEmpresa."IdEmpresa" = CTB_itzPreComprobante."IdEmpresa" 	
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::BAT.ConsultarPrograma" AS Programa ON
					Programa."IdPrograma" = CTB_itzPreComprobante."IdPrograma"
				LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad"  AS EstadoContabilidad ON 
				  	EstadoContabilidad."IdEstadoContabilidad" = CTB_itzPreComprobante."Estado"
			 WHERE (CTB_itzPreComprobante."IdEmpresa" 			= :p_IdEmpresa) 
			   AND (CTB_itzPreComprobante."Estado" 				= :p_Estado 											OR :p_Estado = '')
			   AND (CTB_itzPreComprobante."Estado" != 'A')
			   AND ((CTB_itzPreComprobante."FechaContable" BETWEEN :p_FechaContableInicial AND :p_FechaContableFinal) 	OR :p_FechaContableInicial = '') 
			   AND (CTB_itzPreComprobante."IdTipoContabilidad" 	= :p_IdTipoContabilidad 								OR :p_IdTipoContabilidad = 0) 
			   AND (CTB_itzPreComprobante."FechaProceso" 		= :p_FechaProceso 										OR :p_FechaProceso = '') 
			   AND (CTB_itzPreComprobante."IdPreComprobante" 	= :p_IdPreComprobante 									OR :p_IdPreComprobante = 0) 
			   AND (CTB_itzPreComprobante."FolioContabilidad" 	= :p_FolioContabilidad 									OR :p_FolioContabilidad = 0) 
			   AND (CTB_itzPreComprobante."CodigoUsuario" 		= :p_CodigoUsuario 										OR :p_CodigoUsuario = '') 
			   AND (EstadoContabilidad."NombreEstado" != 'ANULADO')
		  ORDER BY CTB_itzPreComprobante."FechaContable", 
		  		   CTB_itzContabilidadTipo."Nombre", 
		  		   CTB_itzPreComprobante."IdTipoContabilidad", 
		  		   CTB_itzPreComprobante."FechaProceso", 
		  		   CTB_itzPreComprobante."HoraProceso";
	END;
END;