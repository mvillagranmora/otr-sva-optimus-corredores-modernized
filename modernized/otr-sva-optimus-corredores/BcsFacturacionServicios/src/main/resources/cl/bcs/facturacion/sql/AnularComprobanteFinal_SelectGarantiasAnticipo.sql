SELECT
	Car."Cantidad",
	Car."IdCliente",
	Car."IdPortafolio",
	Car."ValorRescate",
	Gta."IdEstado",
	Car."IdInstrumento",
	MovCom."IdComprobante",
	Car."IdTipoComprobante",
	Car."IdModulo",
    Car."Nemotecnico",
    Gta."INGCantidad",
    Car."INGPrecioCompra",
    Gta."EGRCantidad",
    Car."EGRFolioComprobante",
    Car."IdTipoOperacion",
    Gta."IdGarantia",
    Car."FolioCartera",
    'GARANTIA' as "TipoCartera",
    Gta."IdOrigenGarantia",
    Car."IdEmisor",
    Car."FechaVencimientoInstrumento",
    Gta."IdConcepto",
    Gta."INGTasaGarantia" AS "TasaCompra"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MovCom
INNER JOIN "BSCL.Custodia.BD::GTA.Garantia" Gta
	ON Gta."FolioComprobante" = "IdComprobanteReferencia"
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" Car
	ON Gta."IdCarteraOrigen" = Car."IdCartera"
WHERE "IdComprobante" IN (:idsComprobante) 
	AND Gta."IdEstado" IN ('ANT','VIG')
	AND MovCom."IdTipoOperacion" = 'ANSIMC';