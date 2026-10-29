UPDATE "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro"
SET "MontoCobro" = :MontoCobro
, "MontoLiquidacionCobro" = :MontoLiquidacionCobro
, "MontoMovimientoComprobanteCobro" = :MontoMovimientoComprobanteCobro
, "Porcentaje" = :Porcentaje
WHERE "IdMovimientoComprobanteCobro" = :IdMovimientoComprobanteCobro