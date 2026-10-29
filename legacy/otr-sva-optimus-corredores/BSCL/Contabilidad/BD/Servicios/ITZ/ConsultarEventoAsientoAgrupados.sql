SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEventoAsientoAgrupados";
CREATE PROCEDURE 
-- 
--  Proposito: Vista 
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 21/09/2016
--  Id Servicio = 12019
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEventoAsientoAgrupados" (
	IN p_IdPreComprobanteAsiento		BIGINT DEFAULT 0,
	-- Parametros salida del servicio publico
	OUT p_out_TabEventoAsientoAsociado TABLE (	
												"IdTipoEvento"				NVARCHAR(40),
												"Nombre"					NVARCHAR(60),
												"Operador"					NVARCHAR(10),
												"Monto"						DECIMAL
												),
	-- Sección Fija ----
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_ResultadoProceso          TABLE ("Campo" NVARCHAR(40),"Id" BIGINT, "Servicio" NVARCHAR(250), "Resultado" NVARCHAR(40)) DEFAULT EMPTY,
	OUT p_out_AdvertenciasContador      INT,
	OUT p_out_AdvertenciasDetalle       TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador           INT,   
	OUT p_out_ErroresDetalle            TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(100), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
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
	DECLARE v_ErroresDetalle			TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(40), "Descripcion" NVARCHAR(520)) DEFAULT
												SELECT 0 AS "IdComprobante", '' AS "Codigo", '' AS "Descripcion" FROM DUMMY WHERE 1=2;
    DECLARE v_RechazadosPorValidacion	TABLE("IdComprobante" BIGINT) DEFAULT
    											SELECT 0 AS "IdComprobante" FROM DUMMY WHERE 1=2;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN		
		v_hash = SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "CodigoDato1", '' AS "ValorDato1", '' AS "CodigoDato2", '' AS "ValorDato2" FROM DUMMY;
        v_out_hash = SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "Descripcion" FROM DUMMY;

        Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);

		p_out_ErroresDetalle = SELECT "IdInterno" AS "IdComprobante", "Codigo" AS "Codigo", "Descripcion" AS "Descripcion" FROM :v_out_hash
							   UNION ALL
		                       SELECT * FROM :p_out_ErroresDetalle;

		SELECT COUNT(1)
		  INTO p_out_ErroresContador
		  FROM :p_out_ErroresDetalle;
	END;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt NVARCHAR(40);
		
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError"(::SQL_ERROR_CODE, v_errorExt);

			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle 
								   UNION ALL
								   SELECT 0 AS "IdComprobante", v_errorExt, ::SQL_ERROR_MESSAGE FROM DUMMY;
			SELECT COUNT(1)
			  INTO p_out_ErroresContador
			  FROM :p_out_ErroresDetalle;
		ELSE
			--Error interno no manejado (El log será captado por la capa de XS control)
			RESIGNAL;
		END IF;
	END;	

	p_out_AdvertenciasContador 	:= 0;
	p_out_ErroresContador		:= 0;

	BEGIN
		p_out_TabEventoAsientoAsociado = 
				 SELECT 
						CTB_itzEvento."IdTipoEvento" 									AS "IdTipoEvento",
						CTB_itzEventoTipo."Nombre" 										AS "Nombre",
						CTB_itzEventoPreComprobanteAsiento."Operador" 					AS "Operador",
						SUM(ABS(CTB_itzEvento."Monto")) 								AS "Monto"
						--SUM(ABS(CAST(CTB_itzEvento."Monto" AS DECIMAL))) 				AS "Monto"
				FROM "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" AS CTB_itzEventoPreComprobanteAsiento
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Evento" AS CTB_itzEvento ON 
				   						CTB_itzEventoPreComprobanteAsiento."IdEvento" = CTB_itzEvento."IdEvento"
				   				LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipo" AS CTB_itzEventoTipo ON
				   						CTB_itzEventoTipo."IdTipoEvento" = CTB_itzEvento."IdTipoEvento"
				  WHERE (CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento" = :p_IdPreComprobanteAsiento)
				  GROUP BY 						
						CTB_itzEvento."IdTipoEvento" 									
						,CTB_itzEventoTipo."Nombre" 									
						,CTB_itzEventoPreComprobanteAsiento."Operador";
				
		  --ORDER BY CTB_itzEventoPreComprobanteAsiento."Operador", CTB_itzEventoTipo."IdTipoEvento";
	END;
END;
