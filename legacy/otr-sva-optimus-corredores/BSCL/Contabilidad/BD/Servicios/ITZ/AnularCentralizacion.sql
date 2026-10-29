SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.AnularCentralizacion";

CREATE PROCEDURE 
-- 
--  Proposito: Anular
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 13/10/2016
--  Id Servicio = 12043
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.AnularCentralizacion" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
	-- Variables de Entrada variables
	IN p_IdCentralizacion			BIGINT,
	-- Parametros de Salida
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
 )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	   
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorhashmsg 				TABLE ("Codigo" NVARCHAR(100), "Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  				NVARCHAR(520);	        	
	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40)) DEFAULT 
												SELECT 0 AS "IdInterno", '' AS "Codigo", 'N' AS "RequiereAutorizacion", '' AS "ValorParametro",'' AS "NombreParametro" FROM DUMMY WHERE 1=2;
	DECLARE v_out_ErroresDetalle		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40)) DEFAULT 
												SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "ValorParametro", '' AS "NombreParametro" FROM DUMMY WHERE 1=2;
	DECLARE v_count_operaciones			BIGINT;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_ErroresContador 
		  FROM :v_out_ErroresDetalle;
		
		SELECT COUNT(1) INTO p_out_AdvertenciasContador 
		  FROM :v_out_AdvertenciasDetalle; 	
		
		v_TablaCodigosMensajes = SELECT "Codigo" 			AS "CodigoMensaje",
								        "NombreParametro" 	AS "CodigoDato",
  								        "ValorParametro" 	AS "ValorDato" 
								   FROM :v_out_ErroresDetalle
								 UNION ALL
								 SELECT "Codigo" 			AS "CodigoMensaje",
								        "NombreParametro" 	AS "CodigoDato",
								        "ValorParametro" 	AS "ValorDato" 
								   FROM :v_out_AdvertenciasDetalle;

		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes, v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = 	SELECT errores."IdInterno",
									   errores."Codigo",
									   msj."Descripcion"
 								  FROM :v_out_ErroresDetalle errores INNER JOIN :v_out_ErroresDetalleMensajes msj on 
 								  														errores."Codigo" = msj."Codigo";

		p_out_AdvertenciasDetalle = SELECT adv."IdInterno",
									       adv."Codigo",
 									       adv."RequiereAutorizacion",
 									       msj."Descripcion"
 								    FROM :v_out_AdvertenciasDetalle adv INNER JOIN :v_out_ErroresDetalleMensajes msj on 
 								    													adv."Codigo" = msj."Codigo";
	END;

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;

	--------------------
	--Negocio Aqui
	--------------------
	BEGIN -- proceso de negocio      
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		DECLARE v_TienePreComprobante		BIGINT;
		DECLARE v_TieneEvento				BIGINT;
		DECLARE v_GlosaError				NVARCHAR(200);

		--*******************************
		--*VALIDACION REGLAS BASICAS *
		--*******************************
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa
		                                                                 ,:p_IdUsuario
		                                                                 ,:p_IdModulo
		                                                                 ,:v_out_ErroresDetalle_Basicos
		                                                                 ,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 	AS "IdInterno",
			                              '' 	AS "Codigo",
			                              '' 	AS "ValorParametro",
			                              '' 	AS "NombreParametro"
			                         FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT COUNT(1)
		  INTO v_TieneEvento
		  FROM "BSCL.Contabilidad.BD::ITZ.Evento"
		 WHERE "IdCentralizacion" = :p_IdCentralizacion;
		
		IF :v_TieneEvento > 0 THEN
			SELECT COUNT(1) --TOP 1 @TienePreComprobante = 'S'
			  INTO v_TienePreComprobante
			  FROM "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" --[CTB_itzEventoPreComprobanteAsiento]
			 WHERE "IdEvento" IN (SELECT "IdEvento"
									FROM "BSCL.Contabilidad.BD::ITZ.Evento"--[CTB_itzEvento]
								   WHERE "IdCentralizacion" = :p_IdCentralizacion --3228
								)
			   AND "Estado" IN ('C','V');
	
			IF :v_TienePreComprobante = 0 THEN
				UPDATE "BSCL.Contabilidad.BD::ITZ.Evento"
				   SET "Estado" = 'A' 
				 WHERE "IdCentralizacion" = :p_IdCentralizacion;
				
				UPDATE "BSCL.Contabilidad.BD::ITZ.MovimientoDato" AS CTB_ItzMovimientoDato
				   SET "Estado" = 'A'
				  FROM "BSCL.Contabilidad.BD::ITZ.MovimientoDato" AS CTB_ItzMovimientoDato
				  		INNER JOIN "BSCL.Contabilidad.BD::ITZ.Movimiento" AS CTB_ItzMovimiento ON 
				  				CTB_ItzMovimientoDato."IdMovimiento" = CTB_ItzMovimiento."IdMovimiento" 
				  			AND CTB_ItzMovimiento."IdCentralizacion" = :p_IdCentralizacion;
				
				UPDATE "BSCL.Contabilidad.BD::ITZ.Movimiento"
				   SET "Estado" = 'A'
				 WHERE "IdCentralizacion" = :p_IdCentralizacion;
				
				UPDATE "BSCL.Contabilidad.BD::ITZ.Centralizacion"
				   SET "Estado" = 'A'
				 WHERE "IdCentralizacion" = :p_IdCentralizacion;
			ELSE
				v_out_ErroresDetalle = 
				SELECT :p_IdCentralizacion AS "IdInterno", 'CENTRALIZACION_ESTA_CONTABILIZADA' AS "Codigo", TRIM(CAST(:p_IdCentralizacion AS NVARCHAR(40))) AS "ValorParametro", 'IdCentralizacion' AS "NombreParametro" FROM DUMMY;
	
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		ELSE
			UPDATE "BSCL.Contabilidad.BD::ITZ.Centralizacion"
			   SET "Estado" = 'A'
			 WHERE "IdCentralizacion" = :p_IdCentralizacion;
		
			v_out_AdvertenciasDetalle = 
			SELECT :p_IdCentralizacion AS "IdInterno", 'CENTRALIZACION_SIN_EVENTOS' AS "Codigo", 'ADVE' AS "RequiereAutorizacion", TRIM(CAST(:p_IdCentralizacion AS NVARCHAR(40))) AS "ValorParametro", 'IdCentralizacion' AS "NombreParametro" FROM DUMMY;

			SIGNAL SALIDA_CONTROLADA;
		END IF;
	END;
END;