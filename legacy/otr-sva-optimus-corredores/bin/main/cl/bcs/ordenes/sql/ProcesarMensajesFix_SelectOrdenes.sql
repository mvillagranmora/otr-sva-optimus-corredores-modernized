SELECT "IdOrden","IdTipoInstrumento","IdTipoCanalDistribucion","IdTipoCanalIngreso"
FROM "BSCL.Orden.BD::ORD.Orden"
WHERE "IdOrden" IN (:idsOrden);