SELECT
	"IdEstado",
	"Cantidad",
	"INGCantidad" ,
	"EGRCantidad" ,
	"Nemotecnico",
	"IdPortafolio",
	"FolioCartera",
	"FolioComprobante",
	"EGRFolioComprobante",
	"IdPortafolio" ,
	"IdPortafolioRelacionado"
FROM
	"BSCL.Custodia.BD::CUS.Cartera" CA
WHERE
	CA."IdInstrumento" = :IdInstrumento
	AND CA."IdPortafolio" IN (:IdPortafolio)
	AND "IdEstado" = 'VIG';