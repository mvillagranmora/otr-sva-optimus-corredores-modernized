SELECT TRA."IdTransaccion", TRA."Monto" AS "MontoTransaccion", TRA."FechaOperacion" AS "FechaTransaccion", TRA."Precio" AS "PrecioTransaccion", TRA."Cantidad" AS "CantidadTransaccion", TRA."Nemotecnico", TRA."IdTipoOperacion",
TRA."IdClasificacionTipoInstrumento",TRA."IdLibroOrdenes", TRA."IdTipoInstrumento",TRA."IdInstrumento", TRA."IdBolsa",TRA."IdCondicionDeLiquidacion",TRA."EsVentaCorta",
OPT."AccionOrden", TRA."IdMoneda" AS "IdMoneda"
FROM "BSCL.Orden.BD::ORD.Transaccion" TRA
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" OPT ON OPT."IdTipoOperacion" = TRA."IdTipoOperacion"
WHERE "IdTransaccion" IN (:idsTransacciones);