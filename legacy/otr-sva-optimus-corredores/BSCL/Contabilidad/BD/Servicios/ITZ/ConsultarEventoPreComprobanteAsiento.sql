SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEventoPreComprobanteAsiento";
CREATE PROCEDURE 
-- 
--  Proposito: Vista 
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 10/09/2016
--  Id Servicio = 12012
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarEventoPreComprobanteAsiento" (
																			IN p_IdPreComprobanteAsiento	BIGINT	DEFAULT 0,
	-- Parametros salida del servicio publico
	OUT p_out_TabEventoPreComprobanteAsiento TABLE ("IdEvento"				BIGINT,
													"Operador"				NVARCHAR(10),
													"DescOperacion"			NVARCHAR(5),
													"IdTipoEvento"			NVARCHAR(40),
													"Evento"				NVARCHAR(60),
													"Estado"				NVARCHAR(1)
	                                     			),
	-- Sección Fija ----
	--Salidas para mensajes de advertencias y errores
	OUT p_out_ResultadoProceso          TABLE ("Campo" NVARCHAR(40), "Id" BIGINT, "Servicio" NVARCHAR(250), "Resultado" NVARCHAR(40)) DEFAULT EMPTY,
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
		p_out_TabEventoPreComprobanteAsiento = 
				SELECT CTB_itzEventoPreComprobanteAsiento."IdEvento" 				AS "IdEvento", 
					   CTB_itzEventoPreComprobanteAsiento."Operador"				AS "Operador",
					   CASE TRIM(CTB_itzEventoPreComprobanteAsiento."Operador")
					   		WHEN '+' THEN 'SUMA'
					   		WHEN '-' THEN 'RESTA'
					   END															AS "DescOperacion",
					   CTB_itzEventoTipo."IdTipoEvento"								AS "IdTipoEvento",
					   CASE WHEN CTB_itzEventoTipo."Nombre" = '' THEN
					   		'<COMPUESTO>'
					   ELSE
					   		CTB_itzEventoTipo."Nombre"
					   END 															AS "Evento",
					   CTB_itzEventoPreComprobanteAsiento."Estado"					AS "Estado"
				  FROM "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" AS CTB_itzEventoPreComprobanteAsiento
				  						INNER JOIN "BSCL.Contabilidad.BD::ITZ.Evento" AS CTB_itzEvento ON 
				  											CTB_itzEvento."IdEvento" = CTB_itzEventoPreComprobanteAsiento."IdEvento" 
				  						INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipo" AS CTB_itzEventoTipo ON 
				  											CTB_itzEventoTipo."IdTipoEvento" = CTB_itzEvento."IdTipoEvento" 
				 WHERE (CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento" = :p_IdPreComprobanteAsiento OR :p_IdPreComprobanteAsiento = 0)
				 AND  CTB_itzEventoPreComprobanteAsiento."Estado" = 'V';	
	END;
END;