SELECT "IdMatrizAgrupadorAPV", "NombreProductoAgrupador" 
FROM "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" 	AS MAT
INNER JOIN "BSCL.APV.BD::APV.ProductoAgrupador" 	AS ProductoAgrupador
ON ProductoAgrupador."IdProductoAgrupador" = MAT."IdProductoAgrupador"