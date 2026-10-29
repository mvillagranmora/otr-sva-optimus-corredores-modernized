SELECT ORD."IdOrden", ORD."MontoOrden", ORD."FechaRecepcion" AS "FechaOrden", ORD."PrecioOrden", ORD."Cantidad" AS "CantidadOrden", ORD."Nemotecnico", ORD."IdTipoOperacion", ORD."IdTipoPrecio",
ORD."IdClasificacionTipoInstrumento",ORD."IdLibroOrdenes", ORD."IdTipoInstrumento",ORD."IdInstrumento", ORD."IdBolsa",ORD."IdCondicionDeLiquidacion",ORD."EsVentaCorta",
OPT."AccionOrden", ORD."IdMonedaOrden" AS "IdMonedaOrden"
FROM "BSCL.Orden.BD::ORD.Orden" ORD
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" OPT ON OPT."IdTipoOperacion" = ORD."IdTipoOperacion"
WHERE "IdOrden" IN (:idsOrdenes);