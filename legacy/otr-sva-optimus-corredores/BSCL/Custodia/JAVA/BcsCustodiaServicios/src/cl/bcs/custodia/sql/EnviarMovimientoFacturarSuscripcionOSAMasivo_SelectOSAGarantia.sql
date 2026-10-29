SELECT 
	"FolioGarantia"
	,"IdGarantia"
	,"IdInstrumento"
	,"IdPortafolio"
	,"INGCantidad"
	,"Nemotecnico"
	,"IdEstado"
	,TO_VARCHAR("FechaOperacion", 'YYYY-MM-DD') AS "FechaOperacion"
FROM "BSCL.Custodia.BD::GTA.Garantia" 
WHERE "IdPortafolio"  IN (:IdsPortafolio)
	AND "IdInstrumento" IN (:IdsInstrumentosRelacionados)
	AND "IdEstado" = 'VIG'
	AND "IdConcepto" = 'SUECOS';