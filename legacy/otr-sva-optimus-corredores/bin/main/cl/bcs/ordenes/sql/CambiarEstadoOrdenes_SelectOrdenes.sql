SELECT "IdOrden","IdEstadoOrden","OrdenVigente","IdTipoCanalDistribucion","IdTipoCanalIngreso"
FROM "BSCL.Orden.BD::ORD.Orden"
WHERE "IdOrden" IN (:idsOrden)