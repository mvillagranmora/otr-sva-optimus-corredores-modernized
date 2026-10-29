SELECT
	Car."Cantidad",
	Car."IdCliente",
	Car."IdPortafolio",
	Car."ValorRescate",
	Car."IdEstado",
	Car."IdInstrumento",
	Car."FolioComprobante",
	Car."IdTipoComprobante",
	Car."IdModulo",
    Car."Nemotecnico",
    Car."INGCantidad",
    Car."INGPrecioCompra",
    Car."EGRCantidad",
    Car."EGRFolioComprobante",
    Car."IdTipoOperacion",
    Gta."IdGarantia",
    Car."FolioCartera",
    'GARANTIA' as "TipoCartera",
    Gta."IdOrigenGarantia",
    Car."IdEmisor",
    Car."FechaVencimientoInstrumento",
    Car."IdConcepto"
FROM "BSCL.Custodia.BD::CUS.Cartera" Car
INNER JOIN "BSCL.Custodia.BD::GTA.Garantia" Gta
	On Gta."IdCarteraOrigen" = Car."IdCartera"
WHERE Car."IdModulo" = 'FAC' 
	AND Car."IdEstado" = 'LIQ'   
	AND Car."FolioComprobante" IN (:idsComprobante) 
  
UNION ALL
--obtener la garantia de la venta de reportes.
SELECT
	Gta."Cantidad",
	Car."IdCliente",
	Car."IdPortafolio",
	Car."ValorRescate",
	Car."IdEstado",
	Car."IdInstrumento",
	MovCom."IdComprobante" AS "FolioComprobante",
	Car."IdTipoComprobante",
	Car."IdModulo",
    Car."Nemotecnico",
    Gta."INGCantidad",
    Gta."INGPrecioGarantia" AS "INGPrecioCompra",
    Gta."EGRCantidad",
    Gta."EGRFolioComprobante",
    Car."IdTipoOperacion",
    Gta."IdGarantia",
    Gta."FolioCarteraOrigen" AS "FolioCartera",
    'GARANTIA' as "TipoCartera",
    Gta."IdOrigenGarantia",
    Car."IdEmisor",
    Car."FechaVencimientoInstrumento",
    Car."IdConcepto"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MovCom 
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" Car
	ON MovCom."IdOrden" = Car."EGRIdOperacion" AND Car."EGRIdModulo" = 'ORD'
INNER JOIN "BSCL.Custodia.BD::GTA.Garantia" Gta
	ON Gta."EGRIdOperacion" = Car."EGRIdOperacion"
WHERE MovCom."IdComprobante"  IN (:idsComprobante)
	AND "IdConceptoMovimiento" = 'VNRERV';  