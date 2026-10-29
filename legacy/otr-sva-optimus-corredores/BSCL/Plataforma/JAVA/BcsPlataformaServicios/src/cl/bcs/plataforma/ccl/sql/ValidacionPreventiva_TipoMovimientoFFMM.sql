SELECT DISTINCT S."IdTipoMovimientoFondoMutuo" AS "Valor1"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" S 
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" T 
ON T."IdTipoMovimientoFondoMutuo" = S."IdTipoMovimientoFondoMutuo"
WHERE (T."Descripcion" IS NULL OR T."Descripcion"  = '');