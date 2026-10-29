SELECT "Monto", "Cantidad","IdMoneda", "IdClasificacionTipoInstrumento", "Precio", "ValorRescate"
FROM "BSCL.Orden.BD::ORD.Transaccion"
WHERE "IdTransaccion" = :IdTransaccion