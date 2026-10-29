UPDATE "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" 
SET "MueveCustodia"= 'S',
"EsNominal" = 'N',
"FechaLiquidacion" = CASE WHEN "FechaLiquidacion" <= CURRENT_DATE THEN CURRENT_DATE ELSE "FechaLiquidacion" END
WHERE "IdMovimientoCarteraEnTransito" IN (:IdMovimientoCarteraEnTransito)