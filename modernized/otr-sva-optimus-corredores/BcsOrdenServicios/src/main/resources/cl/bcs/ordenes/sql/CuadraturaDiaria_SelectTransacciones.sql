SELECT
			"TRAN"."IdClasificacionTipoInstrumento",
			'GESTION' AS "TipoRegistro",
			CAST ("TRAN"."FechaOperacionTransaccion" AS NVARCHAR(10)) 	AS "FechaOperacionTransaccion",
			CAST ("TRAN"."FechaVencimiento" AS date) 					AS "FechaVencimiento",
			"TRAN"."FolioTransaccion",
			"TRAN"."IdBolsa",
			"TRAN"."AccionOrden",
			"TRAN"."InstrumentoUI" AS "Nemotecnico",
			"TRAN"."CodigoOperadorCorredor",			
			"TRAN"."CantidadTransaccion"  ,
			"TRAN"."CantidadAsignadaTransaccion" ,
			"TRAN"."MontoTransaccion",
			"TRAN"."MontoAsignadoTransaccion",
			"TRAN"."IdMonedaTransaccion",
			"TRAN"."EsFungible",
			CASE
				WHEN "EsFungible" = 'S' THEN 
					CASE
						
						WHEN "ValorRescateAsignadoTransaccion" = 0 THEN 'UI_SIN_MONTO_RESCATE_ASIGNACION'
						WHEN "ValorRescateTransaccion" > "ValorRescateAsignadoTransaccion" THEN 'UI_FALTA_MONTO_RESCATE_ASIGNACION'
						WHEN "ValorRescateTransaccion" < "ValorRescateAsignadoTransaccion" THEN 'UI_SOBRA_ASIGNACION'
						WHEN "MontoAsignadoTransaccion" = 0 THEN 'UI_SIN_MONTO_ASIGNADO'
						WHEN "MontoTransaccion" > "MontoAsignadoTransaccion" THEN 'UI_FALTA_MONTO_ASIGNACION'
						WHEN "MontoTransaccion" < "MontoAsignadoTransaccion" THEN 'UI_SOBRA_MONTO_ASIGNACION'
						ELSE ''
					END
				ELSE
					CASE
						WHEN "CantidadAsignadaTransaccion" = 0 THEN 'UI_SIN_ASIGNACION'
						WHEN "CantidadTransaccion" > "CantidadAsignadaTransaccion" THEN 'UI_FALTA_ASIGNACION'
						WHEN "CantidadTransaccion" < "CantidadAsignadaTransaccion" THEN 'UI_SOBRA_ASIGNACION'
						WHEN "MontoAsignadoTransaccion" = 0 THEN 'UI_SIN_MONTO_ASIGNADO'
						WHEN "MontoTransaccion" > "MontoAsignadoTransaccion" THEN 'UI_FALTA_MONTO_ASIGNACION'
						WHEN "MontoTransaccion" < "MontoAsignadoTransaccion" THEN 'UI_SOBRA_MONTO_ASIGNACION'
						ELSE ''
					END
			END  AS "Descripcion",
			"TRAN"."IdTipoInstrumento",
			"TRAN"."ValorRescateTransaccion",
			"TRAN"."ValorRescateAsignadoTransaccion",
			"TRAN"."IndicadorOrdenDirecta",
			"TRAN"."IdTipoOperacion"
		FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccion" AS "TRAN"
		WHERE "TRAN"."FechaSistemaCreacion" = :FechaInicio
		  AND "TRAN"."IdEstadoTransaccion" != 'ANU'
		  AND "TRAN"."IdClasificacionTipoInstrumento" in (:IdClasificacionTipoInstrumento)
UNION ALL 
SELECT
			"TRAN"."IdClasificacionTipoInstrumento",
			'HISTORICO' AS "TipoRegistro",
			CAST ("TRAN"."FechaOperacionTransaccion" AS NVARCHAR(10)) 	AS "FechaOperacionTransaccion",
			CAST ("TRAN"."FechaVencimiento" AS date) 					AS "FechaVencimiento",
			"TRAN"."FolioTransaccion",
			"TRAN"."IdBolsa",
			"TRAN"."AccionOrden",
			"TRAN"."InstrumentoUI" AS "Nemotecnico",
			"TRAN"."CodigoOperadorCorredor",			
			"TRAN"."CantidadTransaccion"  ,
			"TRAN"."CantidadAsignadaTransaccion" ,
			"TRAN"."MontoTransaccion",
			"TRAN"."MontoAsignadoTransaccion",
			"TRAN"."IdMonedaTransaccion",
			"TRAN"."EsFungible",
			CASE
				WHEN "TipoRegistro" = 'ING'  THEN 'UI_TRANSACCION_INGRESADA_NO_PROCESADA'
				WHEN "TipoRegistro" = 'MOD'  THEN 'UI_TRANSACCION_MODIFICADA_NO_PROCESADA'
				WHEN "TipoRegistro" = 'ANU'  THEN 'UI_TRANSACCION_ANULADA_NO_PROCESADA'
				ELSE ''
			END  AS "Descripcion",
			"TRAN"."IdTipoInstrumento",
			"TRAN"."ValorRescateTransaccion",
			"TRAN"."ValorRescateAsignadoTransaccion",
			"TRAN"."IndicadorOrdenDirecta",
			"TRAN"."IdTipoOperacion"
		FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionHistorica" AS "TRAN"
		WHERE "TRAN"."FechaSistemaCreacion" = :FechaInicio
		  AND "TRAN". "IdEstadoRegistro" = 'PEN'
		  AND "TRAN"."IdClasificacionTipoInstrumento" in (:IdClasificacionTipoInstrumento);
		  --AND "TRAN"."IdBolsa" = :IdBolsa;
