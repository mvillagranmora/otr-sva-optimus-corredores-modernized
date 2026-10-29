SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEventosAsiento";
CREATE PROCEDURE 
-- 
--  Proposito: Vista 
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 10/09/2016
--  Id Servicio = 12011
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEventosAsiento" (
	IN p_IdTipoEvento					BIGINT			default 0,
	IN p_IdDefinicionAsiento			BIGINT			default 0,
	IN p_Operador						NVARCHAR(10)	default '',
	IN p_Estado							NVARCHAR(1)		default '',
	-- Parametros salida del servicio publico
	OUT p_out_TabEventosAsiento TABLE ( 
										"IdTipoEvento"			NVARCHAR(40),
										"IdDefinicionAsiento"	BIGINT,
										"Operador"				NVARCHAR(10),
										"Estado"				NVARCHAR(1),
										"Nombre"				NVARCHAR(60)
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
	DECLARE v_ErroresDetalle			TABLE("IdComprobante" BIGINT, "Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520)); 	
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

	BEGIN
		p_out_TabEventosAsiento = 
				SELECT  CTB_itzContabilidadDefinicionTipoEvento."IdTipoEvento" 			AS "IdTipoEvento",
						CTB_itzContabilidadDefinicionTipoEvento."IdDefinicionAsiento"	AS "IdDefinicionAsiento",
						CTB_itzContabilidadDefinicionTipoEvento."Operador"				AS "Operador",
						CTB_itzContabilidadDefinicionTipoEvento."Estado"				AS "Estado",
						CTB_itzEventoTipo."Nombre"										AS "Nombre"
				   FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionTipoEvento" AS CTB_itzContabilidadDefinicionTipoEvento
				   						INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipo" AS CTB_itzEventoTipo ON
				   											CTB_itzEventoTipo."IdTipoEvento" = CTB_itzContabilidadDefinicionTipoEvento."IdTipoEvento"
				   										--AND CTB_itzEventoTipo."Estado" = 'V'
				  WHERE (CTB_itzContabilidadDefinicionTipoEvento."IdTipoEvento" 		= p_IdTipoEvento 		OR p_IdTipoEvento = 0) 
				    AND (CTB_itzContabilidadDefinicionTipoEvento."IdDefinicionAsiento" 	= p_IdDefinicionAsiento OR p_IdDefinicionAsiento = 0) 
				    AND (CTB_itzContabilidadDefinicionTipoEvento."Operador" 			= p_Operador 			OR p_Operador = '') 
				    AND (CTB_itzContabilidadDefinicionTipoEvento."Estado" 				= p_Estado 				OR p_Estado = '');
	END;
END;