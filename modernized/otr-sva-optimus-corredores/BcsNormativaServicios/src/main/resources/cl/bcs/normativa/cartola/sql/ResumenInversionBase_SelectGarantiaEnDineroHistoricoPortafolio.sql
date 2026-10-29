SELECT
GTA."IdCliente"
,COALESCE(GTA."IdPortafolio",0) AS "IdPortafolio"
,'CAJA' as "Grupo"
,'Garantias en efectivo ' || "IdMonedaInstrumento" as "Item"
,"IdMonedaInstrumento" AS "IdMonedaOperacion"
,TO_DECIMAL(SUM("INGMontoGarantiaActualMonedaInstrumento"),28,6) as "MontoMonedaOperacion"
FROM "BSCL.Custodia.BD::HIS.Garantia" GTA
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON GTA."IdPortafolio" = P."IdPortafolio"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = GTA."IdPortafolio" AND Portafolio."IdCliente" = GTA."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
WHERE GTA."IdOrigenGarantia" = 'CTA'
AND GTA."FechaRespaldo" = :FechaHasta
AND GTA."IdEstado" = 'VIG'
AND GTA."INGFechaLiquidacion" <= :FechaHasta
AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
AND P."IdTrabajo" = :IdTrabajo
GROUP BY
	GTA."IdCliente"
   ,GTA."IdPortafolio"
   ,GTA."IdMonedaInstrumento";