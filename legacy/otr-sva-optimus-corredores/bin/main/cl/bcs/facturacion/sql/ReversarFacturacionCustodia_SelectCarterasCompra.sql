WITH EjecucionContinuaSQL AS (
	SELECT "habilitado"
	FROM "BSCL.Plataforma.BD::BAT.Agenda"
	WHERE "IdTipoPrograma" = 'EjecucionContinuaSQL' 
	AND "habilitado" = 'S'
),
carterasComprobante AS (
	SELECT
		DENSE_RANK() over (PARTITION BY CAR."FolioCartera" ORDER BY CAR."IdCartera" DESC) AS "Rank",
		CAR."Cantidad",CAR."IdCliente",CAR."IdPortafolio",CAR."ValorRescate",CAR."IdEstado",CAR."IdInstrumento",
		CAR."FolioComprobante",CAR."IdTipoComprobante",CAR."IdModulo",CAR."Nemotecnico",CAR."INGCantidad",
		CAR."INGValorRescate",CAR."EGRCantidad",CAR."EGRFolioComprobante",CAR."IdTipoOperacion",CAR."IdCartera",
		CAR."FolioCartera",'CARTERA' as "TipoCartera",CAR."EGRValorRescate",CAR."EsFungible",
		CAR."INGFechaLiquidacion", CAR."IdEmisor", CAR."IdTipoReajuste",CAR."INGPrecioCompra",CAR."INGMontoATasaCompraActual", 
		CAR."IdClasificacionTipoInstrumento", CAR."IdBoveda", CAR."IdCondicionLiquidacion", CAR."IdTipoProducto", CAR."IdMonedaInstrumento", CAR."IdMonedaEmision",
		CAR."IdTipoCanalIngreso", CAR."CorteMinimo", CAR."FolioExternoReferencia", CAR."IdSucursal", CAR."Glosa", CAR."NemoSVS", CAR."FechaVencimientoOperacion", CAR."FechaVencimientoInstrumento",
		CAR."FechaOperacion", CAR."IdCuentaCustodioPortafolio", CAR."IdClienteRelacionado", CAR."IdPortafolioRelacionado", COMP."EsVentaCorta"
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
		LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP 
			ON COMP."IdComprobante" = CAR."FolioComprobante"
		LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS TRANS
			ON TRANS."FolioComprobante" = CAR."FolioComprobante"
			AND COMP."IdComprobante" = TRANS."FolioComprobante" 
			AND TRANS."IdMovimientoCarteraEnTransito" = CAR."IdMovimientoCarteraEnTransito"
		LEFT JOIN EjecucionContinuaSQL AS EJC
			ON EJC."habilitado" = 'S'
	WHERE (CAR."IdCarga" > 0 AND CAR."IdModulo" = 'CUS' AND CAR."FolioComprobante" IN (:idsComprobante)) 
		OR (CAR."IdModulo" = 'FAC'  AND CAR."FolioComprobante" IN (:idsComprobante))
		OR (
			CAR."FolioComprobante" IN (:idsComprobante)
			AND EJC."habilitado" IS NOT NULL
			AND TRANS."EsNominal" = 'N' 
			AND COMP."IdTipoComprobante" = 'FAE' 
			AND COMP."IdFormaCustodia" IN ('NOM','') 
			AND COMP."IdClasificacionTipoInstrumento" IN ('RV','RF','IF')
		)
)
SELECT
"Cantidad","IdCliente","IdPortafolio","ValorRescate","IdEstado","IdInstrumento",
"FolioComprobante","IdTipoComprobante","IdModulo","Nemotecnico","INGCantidad",
"INGValorRescate","EGRCantidad","EGRFolioComprobante","IdTipoOperacion","IdCartera",
"FolioCartera","TipoCartera","EGRValorRescate","EsFungible",
"INGFechaLiquidacion", "IdEmisor", "IdTipoReajuste","INGPrecioCompra","INGMontoATasaCompraActual", "IdClasificacionTipoInstrumento", 
"IdBoveda", "IdCondicionLiquidacion", "IdTipoProducto", "IdMonedaInstrumento", "IdMonedaEmision", "IdTipoCanalIngreso", "CorteMinimo",
"FolioExternoReferencia", "IdSucursal", "Glosa", "NemoSVS", "FechaVencimientoOperacion", "FechaOperacion", "IdCuentaCustodioPortafolio", "FechaVencimientoInstrumento",
"IdClienteRelacionado", "IdPortafolioRelacionado", "EsVentaCorta"
FROM carterasComprobante
WHERE "Rank" = 1