-- Seleccionar todas las matrices con mismo ProductoAgrupador
SELECT "IdMatrizAgrupadorAPV" AS "IdMatrizAgrupadorAPV"
FROM "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS ConsultaMatriz
WHERE ConsultaMatriz."IdProductoAgrupador" IN (:IdsProductoAgrupador)
