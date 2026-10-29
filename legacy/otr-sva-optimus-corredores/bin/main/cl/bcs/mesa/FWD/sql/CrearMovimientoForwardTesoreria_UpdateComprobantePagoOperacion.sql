UPDATE "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
SET "FechaLiquidacionComprobantePagoOperacion" = :FechaAnticipo
WHERE "IdComprobantePagoOperacion" = :IdComprobantePagoOperacion;