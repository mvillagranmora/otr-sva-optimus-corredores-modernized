SELECT 
	"IdCarteraNegativa" as "IdCartera",
	"Cantidad",
	"PrecioOperacion" as "PrecioCompra",
	"IdCliente",
	"IdPortafolio",
    "IdInstrumento",
    "FolioComprobante",
    "IdTipoComprobante",
    "Nemotecnico",
    "FolioComprobante",
    "IdTipoOperacion",
    "EsFungible",
    "IdEmisor",
    "IdTipoReajuste",
    "IdMonedaEmision",
    "FechaLiquidacion" AS "INGFechaLiquidacion",
    "IdConcepto",
    "TasaOperacion" AS "TasaCompra"
FROM "BSCL.Custodia.BD::CUS.CarteraNegativa" NEG
WHERE "IdEstado" = 'VIG' AND "FolioComprobante" IN (:idsComprobante) AND "IdTipoComprobante" = 'FAE'