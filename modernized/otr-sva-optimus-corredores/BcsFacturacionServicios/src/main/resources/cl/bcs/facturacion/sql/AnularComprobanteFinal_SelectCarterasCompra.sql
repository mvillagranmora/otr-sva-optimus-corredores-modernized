WITH
	Cartera AS (
	SELECT
	DENSE_RANK() over (PARTITION BY "FolioCartera" ORDER BY "FolioCartera", "IdCartera" DESC) AS "CarteraPrincipal",
	"Cantidad",
	"IdCliente",
	"IdPortafolio",
	"ValorRescate",
	"IdEstado",
	"IdInstrumento",
	"FolioComprobante",
	"IdTipoComprobante",
	"IdModulo",
	"Nemotecnico",
    "INGCantidad",
    "INGPrecioCompra",
    "EGRCantidad",
    "EGRFolioComprobante",
    "IdTipoOperacion",
    "IdCartera",
    "FolioCartera",
    'CARTERA' as "TipoCartera",
    "IdEmisor",
    "FechaVencimientoInstrumento",
    "IdConcepto",
    "EsFungible"
FROM "BSCL.Custodia.BD::CUS.Cartera"
WHERE "IdModulo" = 'FAC' 
	AND "IdEstado" != 'RPA'
	AND ("IdTipoOperacion" != 'COMSIM' OR ("IdTipoOperacion" = 'COMSIM' AND "IdEstado" = 'VIG'))
	AND "FolioComprobante" IN (:idsComprobante)
)
SELECT * FROM Cartera WHERE  "CarteraPrincipal" = 1