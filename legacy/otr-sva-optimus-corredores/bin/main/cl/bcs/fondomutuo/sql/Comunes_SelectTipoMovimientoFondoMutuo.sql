SELECT 
"IdTipoMovimientoFondoMutuo",
"Descripcion",
"IndicadorAporteRescate",
"IdTipoOperacion",
"TieneCarga",
"TieneIngresoManual",
"TieneCertificacion",
"IdEstado"
FROM "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo"
WHERE "IdTipoMovimientoFondoMutuo" IN (:IdTipoMovimientoFondoMutuo);