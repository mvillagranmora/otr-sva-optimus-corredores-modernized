WITH
	CteMovimientoCarteraEnTransito AS (
		SELECT
			Movimiento."IdMovimientoCarteraEnTransito"
		FROM
			"BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" Movimiento
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TipoOperacion ON (Movimiento."IdTipoOperacion" = TipoOperacion."IdTipoOperacion")
		WHERE
				Movimiento."IdEmpresa" = :IdEmpresa
			AND Movimiento."IdPortafolio" = CASE WHEN :IdPortafolio > 0 THEN :IdPortafolio ELSE Movimiento."IdPortafolio" END
			AND Movimiento."IdEstado" = 'PEN'
			AND Movimiento."IdMovimientoCarteraEnTransitoRelacionado" = 0
			AND Movimiento."IdTipoOperacion" NOT IN ('VESIMC', 'VESIMV')
			AND TipoOperacion."AccionCustodia" IN ('RETIRO', 'INGRESO')		
	)
	, CteTransitoNominalCuentaPropiaTotal AS (
		SELECT
			'CteTransitoNominalCuentaPropiaTotal' AS "TablaOrigen"
			, Movimiento."IdClienteRelacionado" AS "IdCliente"
			, Movimiento."IdPortafolio" AS "IdPortafolio"
			, COALESCE(MovimientoRelacionado."IdInstrumento", Movimiento."IdInstrumento") AS "IdInstrumento"
			, COALESCE(MovimientoRelacionado."IdTipoInstrumento", Movimiento."IdTipoInstrumento") AS "IdTipoInstrumento"
			, COALESCE(MovimientoRelacionado."IdClasificacionTipoInstrumento", Movimiento."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
			, COALESCE(MovimientoRelacionado."IdMonedaInstrumento", '') AS "IdMonedaInstrumento"
			, Movimiento."TasaCompra" AS "Tasa"
			, CASE
				WHEN COALESCE(MovimientoRelacionado."IdClasificacionTipoInstrumento", Movimiento."IdClasificacionTipoInstrumento") = 'IF'
					THEN COALESCE(MovimientoRelacionado."NemoSVS", Movimiento."NemoSVS")
				ELSE COALESCE(MovimientoRelacionado."Nemotecnico", Movimiento."Nemotecnico")
			END AS "Nemotecnico"
			, '' AS "EsPagaderoEn"
			, Movimiento."EsNominal" AS "EsNominal"
			, '' AS "IdTipoOperacion"
			, CASE
				WHEN :IdCliente <> :IdClienteCuentaPropia THEN
					CASE WHEN TipoOperacion."AccionCustodia" = 'INGRESO' THEN 'RETIRO' ELSE 'INGRESO' END
				ELSE TipoOperacion."AccionCustodia"
			END AS "AccionCustodia"
			, CAST(COALESCE(CASE WHEN Movimiento."IdClasificacionTipoInstrumento" = 'IF' THEN Movimiento."ValorRescate" ELSE Movimiento."Cantidad" END, 0) AS DECIMAL(26, 8)) AS "Cantidad"
			, 0 AS "CantidadNominal"
			, Movimiento."MontoCompra" AS "MontoCompra"
			, '' AS "EsVentaDescubierta"
		FROM
			CteMovimientoCarteraEnTransito FiltroMovimiento
			INNER JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" Movimiento ON (Movimiento."IdMovimientoCarteraEnTransito" = FiltroMovimiento."IdMovimientoCarteraEnTransito")
			LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OperacionMovimiento ON (OperacionMovimiento."IdOperacion" = Movimiento."FolioComprobante")
			LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MovimientoRelacionado ON (MovimientoRelacionado."IdMovimientoCarteraEnTransitoRelacionado" = Movimiento."IdMovimientoCarteraEnTransito")
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TipoOperacion ON (TipoOperacion."IdTipoOperacion" = Movimiento."IdTipoOperacion")
		WHERE
				Movimiento."IdClienteRelacionado" = CASE WHEN :IdClienteCuentaPropia > 0 THEN :IdClienteCuentaPropia ELSE Movimiento."IdClienteRelacionado" END
			AND Movimiento."EsNominal" = 'S'
			AND	COALESCE(MovimientoRelacionado."IdClasificacionTipoInstrumento", Movimiento."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(:IdClasificacionTipoInstrumento, '') <> '' THEN :IdClasificacionTipoInstrumento ELSE COALESCE(MovimientoRelacionado."IdClasificacionTipoInstrumento", Movimiento."IdClasificacionTipoInstrumento") END
			AND	COALESCE(MovimientoRelacionado."IdTipoInstrumento", Movimiento."IdTipoInstrumento") = CASE WHEN COALESCE(:IdTipoInstrumento, '') <> '' THEN :IdTipoInstrumento ELSE COALESCE(MovimientoRelacionado."IdTipoInstrumento", Movimiento."IdTipoInstrumento") END
			AND	COALESCE(MovimientoRelacionado."IdInstrumento", Movimiento."IdInstrumento") = CASE WHEN COALESCE(:IdInstrumento, 0) > 0 THEN :IdInstrumento ELSE COALESCE(MovimientoRelacionado."IdInstrumento", Movimiento."IdInstrumento") END
			AND
			(
					(:TipoFechaConsulta = 'LIQ' AND Movimiento."FechaLiquidacion" <= :FechaConsulta)
				OR	(:TipoFechaConsulta = 'ADQ' AND Movimiento."FechaOperacion" <= :FechaConsulta)
			)
	)
	, CteTransitoVentaDescubiertaTotal AS (
		SELECT
			'CteTransitoVentaDescubiertaTotal' AS "TablaOrigen"
			, Movimiento."IdCliente" AS "IdCliente"
			, Movimiento."IdPortafolio" AS "IdPortafolio"
			, Movimiento."IdInstrumento" AS "IdInstrumento"
			, Movimiento."IdTipoInstrumento" AS "IdTipoInstrumento"
			, Movimiento."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
			, COALESCE(Movimiento."IdMonedaInstrumento", '') AS "IdMonedaInstrumento"
			, Movimiento."TasaCompra" AS "Tasa"
			, CASE WHEN Movimiento."IdClasificacionTipoInstrumento" = 'IF' THEN Movimiento."NemoSVS" ELSE Movimiento."Nemotecnico" END AS "Nemotecnico"
			, '' AS "EsPagaderoEn"
			, Movimiento."EsNominal" AS "EsNominal"
			, '' AS "IdTipoOperacion"
			, '' AS "AccionCustodia"
			, CAST(COALESCE(CASE WHEN Movimiento."IdClasificacionTipoInstrumento" = 'IF' THEN Movimiento."ValorRescate" ELSE Movimiento."Cantidad" END, 0) * -1 AS DECIMAL(26, 8)) AS "Cantidad"
			, 0.0 AS "CantidadNominal"
			, Movimiento."MontoCompra" AS "MontoCompra"
			, Movimiento."EsVentaDescubierta" AS "EsVentaDescubierta"
		FROM
			CteMovimientoCarteraEnTransito FiltroMovimiento
			INNER JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" Movimiento ON (Movimiento."IdMovimientoCarteraEnTransito" = FiltroMovimiento."IdMovimientoCarteraEnTransito")
		WHERE
				Movimiento."IdCliente" = CASE WHEN :IdCliente > 0 THEN :IdCliente ELSE Movimiento."IdCliente" END
			AND Movimiento."EsNominal" = 'N'
			AND	Movimiento."IdClasificacionTipoInstrumento" = CASE WHEN COALESCE(:IdClasificacionTipoInstrumento, '') <> '' THEN :IdClasificacionTipoInstrumento ELSE Movimiento."IdClasificacionTipoInstrumento" END
			AND	Movimiento."IdTipoInstrumento" = CASE WHEN COALESCE(:IdTipoInstrumento,'') <> '' THEN :IdTipoInstrumento ELSE Movimiento."IdTipoInstrumento" END
			AND	Movimiento."IdInstrumento" = CASE WHEN COALESCE(:IdInstrumento, 0) > 0 THEN :IdInstrumento ELSE Movimiento."IdInstrumento" END
			AND Movimiento."IdTipoOperacion" NOT IN ('PRESTA', 'EMPRES')
			AND Movimiento."EsVentaDescubierta" = 'S'
	)
SELECT
	"TablaOrigen", "IdCliente", "IdPortafolio", "IdInstrumento", "IdTipoInstrumento"
	, "IdClasificacionTipoInstrumento", "IdMonedaInstrumento", "Nemotecnico", "EsPagaderoEn", "EsNominal"
	, "IdTipoOperacion", "AccionCustodia", "Cantidad", "CantidadNominal", "EsVentaDescubierta"
	, "Tasa"
FROM
(
	SELECT
		"TablaOrigen", "IdCliente", "IdPortafolio", "IdInstrumento", "IdTipoInstrumento"
		, "IdClasificacionTipoInstrumento", "IdMonedaInstrumento", "Nemotecnico", "EsPagaderoEn", "EsNominal"
		, "IdTipoOperacion", "AccionCustodia", "Cantidad", "CantidadNominal", "EsVentaDescubierta"
		, "Tasa"
	FROM CteTransitoNominalCuentaPropiaTotal
	UNION ALL
	SELECT
		"TablaOrigen", "IdCliente", "IdPortafolio", "IdInstrumento", "IdTipoInstrumento"
		, "IdClasificacionTipoInstrumento", "IdMonedaInstrumento", "Nemotecnico", "EsPagaderoEn", "EsNominal"
		, "IdTipoOperacion", "AccionCustodia", "Cantidad", "CantidadNominal", "EsVentaDescubierta"
		, "Tasa"
	FROM CteTransitoVentaDescubiertaTotal
)
