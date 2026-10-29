SELECT
	'CteGarantiaTotal' AS "TablaOrigen"
	, Garantia."IdCliente" AS "IdCliente"
	, Garantia."IdPortafolio" AS "IdPortafolio"
	, Garantia."IdInstrumento" AS "IdInstrumento"
	, Garantia."IdTipoInstrumento" AS "IdTipoInstrumento"
	, Garantia."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
	, Garantia."IdMonedaInstrumento" AS "IdMonedaInstrumento"
	, Garantia."INGTasaGarantia" AS "Tasa"
	, CASE WHEN Garantia."IdClasificacionTipoInstrumento" = 'IF' THEN Garantia."NemoSVS" ELSE Garantia."Nemotecnico" END AS "Nemotecnico"
	, CASE WHEN IFNULL(Cartera."IdMonedaInstrumento", '') = 'UF' THEN 'CLP' ELSE Cartera."IdMonedaInstrumento" END AS "EsPagaderoEn"
	, 'N' AS "EsNominal"
	, '' AS "IdTipoOperacion"
	, '' AS "AccionCustodia"
	, CAST(IFNULL(CASE WHEN Garantia."IdClasificacionTipoInstrumento" = 'IF' THEN Garantia."INGValorRescate" ELSE Garantia."INGCantidad" END, 0) AS decimal(26, 8)) AS "Cantidad"
	, 0.0 AS "CantidadNominal"
	, '' AS "EsVentaDescubierta"
	, Garantia."INGMontoCompraActualMonedaInstrumento" AS "MontoCompra"
FROM
	"BSCL.Custodia.BD::GTA.Garantia" Garantia
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" Cartera ON (Cartera."IdCartera" = Garantia."IdCarteraOrigen")
WHERE
		Garantia."IdEmpresa" = :IdEmpresa
	AND Garantia."IdEstado" = 'VIG'
	AND Garantia."IdCliente" = CASE WHEN :IdCliente > 0 THEN :IdCliente ELSE Garantia."IdCliente" END
	AND Garantia."IdPortafolio" = CASE WHEN :IdPortafolio > 0 THEN :IdPortafolio ELSE Garantia."IdPortafolio" END
	AND Garantia."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(:IdClasificacionTipoInstrumento, '') <> '' THEN :IdClasificacionTipoInstrumento ELSE Garantia."IdClasificacionTipoInstrumento" END
	AND Garantia."IdTipoInstrumento" = CASE WHEN IFNULL(:IdTipoInstrumento, '') <> '' THEN :IdTipoInstrumento ELSE Garantia."IdTipoInstrumento" END
	AND Garantia."IdInstrumento" = CASE WHEN IFNULL(:IdInstrumento, 0) > 0 THEN :IdInstrumento ELSE Garantia."IdInstrumento" END
	AND
    (
			(:TipoFechaConsulta = 'LIQ' AND Garantia."INGFechaLiquidacion" <= :FechaConsulta)
		OR	(:TipoFechaConsulta = 'ADQ' AND Garantia."INGFechaOperacion" <= :FechaConsulta)
	)