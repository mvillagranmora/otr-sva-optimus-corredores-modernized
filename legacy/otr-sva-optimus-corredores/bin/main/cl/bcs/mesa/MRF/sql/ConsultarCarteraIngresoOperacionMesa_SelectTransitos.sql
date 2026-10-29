SELECT 
 "IdCliente" 
,"IdPortafolio"
,"FolioCustodia"
,"Nemotecnico"
,"IdInstrumento"
,SUM("Cantidad") AS "Cantidad"
,SUM("ValorRescate") AS "ValorRescate"
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS TRA
WHERE "IdCliente" IN (:idCliente)
AND "IdPortafolio" IN (:idPortafolio)
AND "IdEstado" = 'PEN'
AND "FolioCustodia" IN (:folioCustodia)
GROUP BY "IdCliente" 
		,"IdPortafolio"
		,"FolioCustodia"
		,"Nemotecnico"
		,"IdInstrumento"
ORDER BY "FolioCustodia" DESC 