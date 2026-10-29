-- Seleccionar ProductoAgrupador de matrices APV
SELECT "IdProductoAgrupador" AS "IdProductoAgrupador"
FROM "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador
WHERE MatrizAgrupador."IdMatrizAgrupadorAPV" IN (:IdsMatrizAgrupadorAPV)
