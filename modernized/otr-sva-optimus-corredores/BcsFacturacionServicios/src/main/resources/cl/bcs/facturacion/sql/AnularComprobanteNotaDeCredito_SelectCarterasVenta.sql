SELECT "Cantidad","INGCantidad","EGRCantidad","IdCliente","IdPortafolio","ValorRescate","INGValorRescate",
"EGRValorRescate",C."IdEstado","IdInstrumento","FolioComprobante","IdTipoComprobante","IdModulo","Nemotecnico",
"EGRFolioComprobante","IdTipoOperacion",S."IdCartera",C."FolioCartera",'CARTERA' as "TipoCartera","EsFungible",
 "INGPrecioCompra","PlazoEnDiasOperacion","IdEmisor","IdTipoReajuste","IdMonedaEmision",'N' AS "EsVentaDescubierta",
    "FechaLiquidacion" AS "INGFechaLiquidacion","IdConcepto","TasaCompra","PrecioAPlazo","MontoAPlazo","IdCondicionLiquidacion",
    "MontoCompraMonedaInstrumento","IdBoveda","IdTipoProducto","IdAgente","IdMonedaInstrumento","IdSucursal","Glosa",
    "IdTipoCanalIngreso","NemoSVS","CorteMinimo","FolioExternoReferencia","IdClasificacionTipoInstrumento",
    "EGRIdTipoOperacion","EGRIdConcepto","EGRIdTipoComprobante"
FROM 
(
	SELECT "IdCartera","IdEstado"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "EGRIdModulo" = 'FAC' 
	AND ("EGRFolioComprobante" IN (:idsComprobante))
	AND "EGRIdTipoOperacion" NOT IN ('VENSIM', 'COMSIM', 'VENRRV','COMRRV')

) S
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" C	ON S."IdCartera" = C."IdCartera"
WHERE C."IdEstado" = 'LIQ' OR (C."IdEstado" = 'RPA' AND "EGRIdModulo" = 'FAC' AND "EGRFolioComprobante" IN (:idsComprobante))
UNION ALL 
SELECT
	"Cantidad","INGCantidad","EGRCantidad","IdCliente","IdPortafolio","ValorRescate","INGValorRescate",
	"EGRValorRescate",C."IdEstado","IdInstrumento","FolioComprobante","IdTipoComprobante","IdModulo","Nemotecnico",
	"EGRFolioComprobante","IdTipoOperacion","IdCartera",S."FolioCartera",'CARTERA' as "TipoCartera","EsFungible",
	"INGPrecioCompra","PlazoEnDiasOperacion","IdEmisor","IdTipoReajuste","IdMonedaEmision",'N' AS "EsVentaDescubierta",
	"FechaLiquidacion" AS "INGFechaLiquidacion","IdConcepto","TasaCompra","PrecioAPlazo","MontoAPlazo","IdCondicionLiquidacion",
	 "MontoCompraMonedaInstrumento","IdBoveda","IdTipoProducto","IdAgente","IdMonedaInstrumento","IdSucursal","Glosa",
	 "IdTipoCanalIngreso","NemoSVS","CorteMinimo","FolioExternoReferencia","IdClasificacionTipoInstrumento",
	 "EGRIdTipoOperacion","EGRIdConcepto","EGRIdTipoComprobante"
FROM 
(
	SELECT "FolioCartera"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "EGRIdModulo" = 'FAC' 
	AND ("EGRFolioComprobante" IN (:idsComprobante))
	AND "EGRIdTipoOperacion" NOT IN ('VENSIM', 'COMSIM', 'VENRRV','COMRRV')
	GROUP BY "FolioCartera"

) S
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" C	ON S."FolioCartera" = C."FolioCartera"
WHERE C."IdEstado" IN ('VIG','ANT');