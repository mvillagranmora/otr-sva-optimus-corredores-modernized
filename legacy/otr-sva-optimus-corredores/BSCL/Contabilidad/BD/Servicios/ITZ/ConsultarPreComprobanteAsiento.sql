SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarPreComprobanteAsiento";
GO
CREATE PROCEDURE 
-- 
--  Proposito: Vista 
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 10/09/2016
--  Id Servicio = 12010
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarPreComprobanteAsiento" (
	IN p_IdEmpresa								NVARCHAR(3)		default '',
	IN p_IdPreComprobante						DECIMAL(18,0)	default 0 ,
	IN p_FechaContable							NVARCHAR(10)	default '',
	IN p_Origen									NVARCHAR(1)		default '',
	-- Parametros salida del servicio publico
	OUT p_out_TabPreComprobanteAsiento TABLE (
												"Glosa"						NVARCHAR(150),
												"IdCuentaContable"			NVARCHAR(12),
												"NombreCuenta"				NVARCHAR(70),
												"Ejercicio"					BIGINT,
												"FechaContable"				NVARCHAR(10),
												"MontoDebe"					DECIMAL,
												"MontoHaber"				DECIMAL,
												"Estado"					NVARCHAR(1),
												"NombreEstado"				NVARCHAR(50),
												"IdPreComprobanteAsiento"	BIGINT,
												"IdPreComprobante"			BIGINT,
												"IdEmpresa"					BIGINT,
												"Linea"						BIGINT,
												"IdTipoContabilidad"		BIGINT,
												"IdDefinicionAsiento"		BIGINT,
												"IdMoneda"					NVARCHAR(3)
			                                     ),
	-- Sección Fija ----
	--Salidas para mensajes de advertencias y errores
	OUT p_out_ResultadoProceso          table ("Campo" VARCHAR(40),"Id" BIGINT, "Servicio" VARCHAR(250), "Resultado" VARCHAR(40)),
	OUT p_out_AdvertenciasContador      int,
	OUT p_out_AdvertenciasDetalle       table ("IdComprobante" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),  
	OUT p_out_ErroresContador           int,   
	OUT p_out_ErroresDetalle            table ("IdComprobante" BIGINT, "Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
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
	DECLARE v_ErroresDetalle			TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520)); 	
    DECLARE v_RechazadosPorValidacion	TABLE("IdComprobante" BIGINT);	        	

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
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select 0 As "IdComprobante",'' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select 0 As "IdComprobante", '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	p_out_ResultadoProceso	    = select '' as "Campo",    0 as "Id", '' as "Servicio", '' as "Resultado" From dummy where 1=2;	
	v_RechazadosPorValidacion   = select 0 As "IdComprobante" from dummy where 1=2;
	
	v_IdMoneda				   :='';
	
	SELECT "Valor" INTO v_IdMoneda 
	FROM "BSCL.Plataforma.BD::PLA.Configuracion"
	WHERE "IdEmpresa" = p_IdEmpresa AND "CodigoParametro" = 'PLA.MonedaSistema';


	BEGIN
		IF p_Origen = 'M' THEN
			p_out_TabPreComprobanteAsiento = 
				SELECT CTB_itzPreComprobanteAsiento."Glosa"													AS "Glosa",
					   CTB_itzPreComprobanteAsiento."IdCuentaContable"										AS "IdCuentaContable",
					   CASE WHEN CTB_itzHomologaCuenta."NombreCuenta" = NULL THEN
					   		'?'
					   ELSE
					   		CTB_itzHomologaCuenta."NombreCuenta"
					   END 																					AS "NombreCuenta",
					   CTB_itzPreComprobanteAsiento."Ejercicio"												AS "Ejercicio",
					   CAST(CAST(CTB_itzPreComprobanteAsiento."FechaContable" AS TIMESTAMP) AS CHAR(10)) 	AS "FechaContable",
					   CAST(CTB_itzPreComprobanteAsiento."MontoDebe" AS decimal) 							AS "MontoDebe",
					   CAST(CTB_itzPreComprobanteAsiento."MontoHaber" AS decimal) 							AS "MontoHaber",
					   CTB_itzPreComprobanteAsiento."Estado"												AS "Estado",
					   EstadoContabilidad."NombreEstado"													AS "NombreEstado",	
					   CTB_itzPreComprobanteAsiento."IdPreComprobanteAsiento" 								AS "IdPreComprobanteAsiento",
					   CTB_itzPreComprobanteAsiento."IdPreComprobante"										AS "IdPreComprobante",
					   CTB_itzPreComprobanteAsiento."IdEmpresa"												AS "IdEmpresa",
					   CTB_itzPreComprobanteAsiento."Linea"													AS "Linea",
					   CTB_itzPreComprobante."IdTipoContabilidad"											AS "IdTipoContabilidad",
					   0 																					AS "IdDefinicionAsiento",
					   --v_IdMoneda																			AS "IdMoneda"
--					   CTB_itzEvento."CodigoMoneda"															AS "IdMoneda"
(	SELECT max("CodigoMoneda")		   
						  	FROM "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento	
						  	INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" AS CTB_itzEventoPreComprobanteAsiento
						  	ON (CTB_itzPreComprobanteAsiento."IdPreComprobanteAsiento" = CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento")
						    LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Evento" AS CTB_itzEvento 
						    ON	CTB_itzEventoPreComprobanteAsiento."IdEvento" = CTB_itzEvento."IdEvento"
							WHERE CTB_itzPreComprobanteAsiento."IdPreComprobante" = CTB_itzPreComprobante."IdPreComprobante" )			AS "IdMoneda"
				  FROM "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento	
-- Agrega Moneda
	/*			  INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" AS CTB_itzEventoPreComprobanteAsiento
				  ON (CTB_itzPreComprobanteAsiento."IdPreComprobanteAsiento" = CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento")
				   LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Evento" AS CTB_itzEvento 
				   ON	CTB_itzEventoPreComprobanteAsiento."IdEvento" = CTB_itzEvento."IdEvento"*/

-- Agrega Moneda				  
				  
				  					INNER JOIN "BSCL.Contabilidad.BD::ITZ.PreComprobante" AS CTB_itzPreComprobante ON 
				  										CTB_itzPreComprobante."IdPreComprobante" = CTB_itzPreComprobanteAsiento."IdPreComprobante" 
				  									AND (CTB_itzPreComprobanteAsiento."MontoDebe" <> 0 OR CTB_itzPreComprobanteAsiento."MontoHaber" <> 0)
				  					LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.HomologaCuenta" AS CTB_itzHomologaCuenta ON
				  										CTB_itzPreComprobanteAsiento."IdCuentaContable" = CTB_itzHomologaCuenta."CuentaOptimus"
				  					LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad"  AS EstadoContabilidad ON 
				  										EstadoContabilidad."IdEstadoContabilidad" = CTB_itzPreComprobanteAsiento."Estado"	 
				 WHERE (CTB_itzPreComprobanteAsiento."IdPreComprobante" = p_IdPreComprobante 	OR p_IdPreComprobante 						 = 0) 
				   AND (CTB_itzPreComprobanteAsiento."FechaContable" 	= p_FechaContable 		OR p_FechaContable 							 = '') 
				   AND (CTB_itzPreComprobanteAsiento."MontoDebe" 		<> 0 					OR CTB_itzPreComprobanteAsiento."MontoHaber" <> 0) 
				ORDER BY CTB_itzPreComprobanteAsiento."IdPreComprobante", CTB_itzPreComprobanteAsiento."Linea";
		END IF;

		IF p_Origen = 'C' THEN
			p_out_TabPreComprobanteAsiento = 
				SELECT CTB_itzPreComprobanteAsiento."Glosa"													AS "Glosa",
					   CTB_itzPreComprobanteAsiento."IdCuentaContable"										AS "IdCuentaContable",
					   CASE WHEN CTB_itzHomologaCuenta."NombreCuenta" = NULL THEN
					   		'?'
					   ELSE
					   		CTB_itzHomologaCuenta."NombreCuenta"
					   END 																					AS "NombreCuenta",
					   CTB_itzPreComprobanteAsiento."Ejercicio"												AS "Ejercicio",
					   CAST(CAST(CTB_itzPreComprobanteAsiento."FechaContable" AS TIMESTAMP) AS CHAR(10)) 	AS "FechaContable",
					   CAST(CTB_itzPreComprobanteAsiento."MontoDebe" AS decimal) 							AS "MontoDebe",
					   CAST(CTB_itzPreComprobanteAsiento."MontoHaber" AS decimal) 							AS "MontoHaber",
					   CTB_itzPreComprobanteAsiento."Estado"												AS "Estado",
					   EstadoContabilidad."NombreEstado"													AS "NombreEstado",	
					   CTB_itzPreComprobanteAsiento."IdPreComprobanteAsiento" 								AS "IdPreComprobanteAsiento",
					   CTB_itzPreComprobanteAsiento."IdPreComprobante"										AS "IdPreComprobante",
					   CTB_itzPreComprobanteAsiento."IdEmpresa"												AS "IdEmpresa",
					   CTB_itzPreComprobanteAsiento."Linea"													AS "Linea",
					   CTB_itzPreComprobante."IdTipoContabilidad"											AS "IdTipoContabilidad",
					   0 																					AS "IdDefinicionAsiento",
					   v_IdMoneda																			AS "IdMoneda"
				  FROM "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento 
				  					INNER JOIN "BSCL.Contabilidad.BD::ITZ.PreComprobante" AS CTB_itzPreComprobante ON 
				  										CTB_itzPreComprobante."IdPreComprobante" = CTB_itzPreComprobanteAsiento."IdPreComprobante" 
				  									AND (CTB_itzPreComprobanteAsiento."MontoDebe" <> 0 OR CTB_itzPreComprobanteAsiento."MontoHaber" <> 0)
				  					LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.HomologaCuenta" AS CTB_itzHomologaCuenta ON
				  										CTB_itzPreComprobanteAsiento."IdCuentaContable" = CTB_itzHomologaCuenta."CuentaOptimus"
				  					LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad"  AS EstadoContabilidad ON 
				  										EstadoContabilidad."IdEstadoContabilidad" = CTB_itzPreComprobanteAsiento."Estado"
				 WHERE (CTB_itzPreComprobanteAsiento."IdPreComprobante" = p_IdPreComprobante 	OR p_IdPreComprobante 						 = 0)
				   AND (CTB_itzPreComprobanteAsiento."FechaContable" 	= p_FechaContable 		OR p_FechaContable							 = '') 
				   AND (CTB_itzPreComprobanteAsiento."MontoDebe" 		<> 0 					OR CTB_itzPreComprobanteAsiento."MontoHaber" <> 0) 
				   AND (CTB_itzPreComprobanteAsiento."Estado" 			IN ('C', 'V', 'D'))
				ORDER BY CTB_itzPreComprobanteAsiento."IdPreComprobante", CTB_itzPreComprobanteAsiento."Linea";
		END IF;
	END;
END;