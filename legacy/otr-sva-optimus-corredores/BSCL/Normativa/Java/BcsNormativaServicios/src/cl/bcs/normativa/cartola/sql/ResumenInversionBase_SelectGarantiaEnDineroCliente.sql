SELECT
GTA."IdCliente"
,COALESCE(GTA."IdPortafolio",0) AS "IdPortafolio"
,'CAJA' as "Grupo"
,'Garantías en efectivo' as "Item"
,"IdMonedaInstrumento" AS "IdMonedaOperacion"
,TO_DECIMAL(SUM("INGMontoGarantiaActualMonedaInstrumento"),28,6) as "MontoMonedaOperacion"
FROM "BSCL.Custodia.BD::GTA.Garantia" GTA
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = GTA."IdPortafolio" AND Portafolio."IdCliente" = GTA."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
WHERE
GTA."IdCliente" IN (:IdCliente)
AND	GTA."IdOrigenGarantia" = 'CTA'
AND GTA."IdEstado" = 'VIG'
AND GTA."INGFechaLiquidacion" <= :FechaHasta
AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
GROUP BY
	GTA."IdCliente"
   ,GTA."IdPortafolio"
   ,GTA."IdMonedaInstrumento";