UPDATE "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
SET "FechaIngresoComprobantePagoOperacion" = :FechaOperacionNueva,
    "FechaLiquidacionComprobantePagoOperacion" = :FechaOperacionNueva
WHERE "IdOperacion" = :IdOperacion