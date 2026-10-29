SELECT
GTA."IdPortafolio"
,GTA."IdInstrumento"
,SUM(GTA."INGCantidad") AS "Cantidad"
FROM "BSCL.Custodia.BD::GTA.Garantia" GTA
WHERE GTA."IdPortafolio" IN (:IdPortafolio)
AND GTA."IdEstado" = 'VIG'
AND GTA."IdClasificacionTipoInstrumento" = 'FM'
GROUP BY GTA."IdPortafolio", GTA."IdInstrumento"