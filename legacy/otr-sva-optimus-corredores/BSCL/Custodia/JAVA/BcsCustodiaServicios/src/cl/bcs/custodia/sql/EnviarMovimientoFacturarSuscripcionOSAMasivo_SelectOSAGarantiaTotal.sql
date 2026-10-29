SELECT
	"IdInstrumento"
	,"IdPortafolio"
	,SUM("INGCantidad") AS "INGCantidad"
FROM "BSCL.Custodia.BD::GTA.Garantia" 
WHERE "IdPortafolio"  IN (:IdsPortafolio)
	AND   "IdInstrumento" IN (:IdsInstrumentosRelacionados)
	AND   "IdEstado" = 'VIG'
GROUP BY 
	"IdInstrumento",
	"IdPortafolio";