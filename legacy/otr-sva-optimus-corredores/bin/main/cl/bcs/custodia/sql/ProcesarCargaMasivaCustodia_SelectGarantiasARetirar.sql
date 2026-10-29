SELECT
	"IdGarantia",
	"Cantidad",
	"IdInstrumento",
	"FolioGarantia"
FROM "BSCL.Custodia.BD::GTA.Garantia"
WHERE "IdEstado" = 'VIG'
	AND "IdEmisor" IN (:IdsEmisor)
	AND "IdCliente" IN (:IdsCliente)
	AND "IdPortafolio" IN (:IdsPortafolio)
	AND "IdInstrumento" IN (:IdsInstrumento)
	AND "FolioGarantia"	IN(:FolioGarantia);