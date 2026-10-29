SELECT "Cantidad","INGCantidad","EGRCantidad","IdCliente","IdPortafolio","ValorRescate","INGValorRescate",
"EGRValorRescate",C."IdEstado","IdInstrumento","FolioComprobante","IdTipoComprobante","IdModulo","Nemotecnico",
"EGRFolioComprobante","IdTipoOperacion",S."IdCartera",C."FolioCartera",'CARTERA' as "TipoCartera","EsFungible",
"INGPrecioCompra", "PlazoEnDiasOperacion", "IdEmisor", "IdTipoReajuste", "IdMonedaEmision", 'N' AS "EsVentaDescubierta", "FechaVencimientoInstrumento", "NemoSVS", "FechaVencimientoOperacion", "IdBoveda", "IdConcepto",
"FechaLiquidacion" AS "INGFechaLiquidacion","INGMontoATasaCompraActual"
FROM 
(
	SELECT "IdCartera"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE ("IdCarga" > 0 AND "EGRIdModulo" = 'CUS' AND "EGRFolioComprobante" IN (:idsComprobante)) OR ("EGRIdModulo" = 'FAC' AND ("EGRFolioComprobante" IN (:idsComprobante)))
	AND "EGRIdTipoOperacion" != 'VENSIM'
) S
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" C ON S."IdCartera" = C."IdCartera"
WHERE C."IdEstado" = 'LIQ' OR ("IdCarga" > 0 AND "EGRIdModulo" = 'CUS' AND "EGRFolioComprobante" IN (:idsComprobante)) OR (C."IdEstado" = 'RPA' AND "EGRIdModulo" = 'FAC' AND "EGRFolioComprobante" IN (:idsComprobante))
UNION ALL
SELECT "Cantidad","INGCantidad","EGRCantidad","IdCliente","IdPortafolio","ValorRescate","INGValorRescate",
"EGRValorRescate",C."IdEstado","IdInstrumento","FolioComprobante","IdTipoComprobante","IdModulo","Nemotecnico",
"EGRFolioComprobante","IdTipoOperacion","IdCartera",S."FolioCartera",'CARTERA' as "TipoCartera","EsFungible",
"INGPrecioCompra", "PlazoEnDiasOperacion", "IdEmisor", "IdTipoReajuste", "IdMonedaEmision", 'N' AS "EsVentaDescubierta", "FechaVencimientoInstrumento", "NemoSVS", "FechaVencimientoOperacion", "IdBoveda", "IdConcepto",
"FechaLiquidacion" AS "INGFechaLiquidacion","INGMontoATasaCompraActual"
FROM 
(
	SELECT "FolioCartera"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE ("IdCarga" > 0 AND "EGRIdModulo" = 'CUS' AND "EGRFolioComprobante" IN (:idsComprobante)) OR ("EGRIdModulo" = 'FAC' AND ("EGRFolioComprobante" IN (:idsComprobante)))
	  AND "EGRIdTipoOperacion" != 'VENSIM'
	GROUP BY "FolioCartera"
) S
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" C ON S."FolioCartera" = C."FolioCartera"
WHERE C."IdEstado" = 'VIG';