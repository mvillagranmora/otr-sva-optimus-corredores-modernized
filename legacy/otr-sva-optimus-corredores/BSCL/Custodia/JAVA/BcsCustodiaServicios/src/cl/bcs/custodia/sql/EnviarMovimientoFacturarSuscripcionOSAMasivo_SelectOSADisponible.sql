SELECT
	"IdInstrumento"
	,"IdPortafolio"
	,SUM("INGCantidad") AS "INGCantidad"
FROM "BSCL.Custodia.BD::CUS.Cartera" 
WHERE "IdPortafolio"  IN (:IdsPortafolio)
	AND   "IdInstrumento" IN (:IdsInstrumentosRelacionados)
	AND   "IdEstado" = 'VIG'
GROUP BY 
	"IdInstrumento",
	"IdPortafolio";