WITH
	CteUtilityViewReservaTotal AS (
		SELECT
			Reserva."FolioCarteraOrigen"     AS "FolioCarteraOrigen"
			, SUM(Reserva."IngCantidad")     AS "IngCantidad"
			, SUM(Reserva."IngValorRescate") AS "IngValorRescate"
			, SUM(Reserva."MontoValorizado") AS "IngMontoValorizado"
		FROM
			"BSCL.Custodia.BD::CUS.Reserva" Reserva
		WHERE
			Reserva."IdEstado" = 'VIG'
		GROUP BY
			Reserva."FolioCarteraOrigen"
	)
	, CteUtilityViewCarteraSubyacenteBasica AS (
		SELECT
			Subyacente."IdSubyacente"
			, Subyacente."IdInstrumento"
			, Subyacente."IdTipoInstrumento"
			, Subyacente."IdClasificacionTipoInstrumento"
			, CASE WHEN IFNULL(Subyacente."IdMonedaInstrumento", '') <> '' THEN IFNULL(Subyacente."IdMonedaInstrumento", '') ELSE COALESCE(Instrumento."IdMoneda", '') END AS "IdMonedaInstrumento"
			, CASE WHEN COALESCE(Subyacente."IdEmisor", Instrumento."IdEmisor", '') <> '' THEN COALESCE(Subyacente."IdEmisor", Instrumento."IdEmisor", '') ELSE Instrumento."IdEmisor" END AS "IdEmisor"
			, Subyacente."NemoSVS"
			, Subyacente."Nemotecnico"
			, Subyacente."INGValorRescate"
			, Subyacente."INGCantidad"
			, Subyacente."FolioCarteraRelacionada"
			, Subyacente."FolioSubyacente"
			, Subyacente."IdEstado"
			, Subyacente."IdCarteraRetirada"
			, Subyacente."INGMontoCompraActualMonedaInstrumento"
		FROM
			"BSCL.Custodia.BD::CUS.Subyacente" Subyacente
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" Instrumento ON (Subyacente."IdInstrumento" = Instrumento."IdInstrumento")
		WHERE
			Subyacente."IdEstado" = 'VIG'
	)
	, CteUtilityViewUltimoMovimientoSubyacente AS (
		SELECT
			Subyacente."FolioCarteraRelacionada" AS "FolioCarteraRelacionada"
			, MAX(Subyacente."IdSubyacente") AS "IdSubyacente"
		FROM
			"BSCL.Custodia.BD::CUS.Subyacente" Subyacente
		WHERE
			"IdEstado" IN ('VIG', 'RPA')
		GROUP BY
			"FolioCarteraRelacionada"
	)
	, CteUtilityViewUltimoMovimientoCartera AS (
		SELECT
			Cartera."IdEmpresa" AS "IdEmpresa"
			, Cartera."FolioCartera" AS "FolioCartera"
			, MAX(Cartera."IdCartera") AS "IdCartera"
		FROM
			"BSCL.Custodia.BD::CUS.Cartera" Cartera
		GROUP BY
			Cartera."IdEmpresa"
			, Cartera."FolioCartera"
	)
	, CteMovimientoComprobanteUnico AS (
	    SELECT
	        RANK() OVER(
	       		PARTITION BY
	       			MovimientoComprobante."IdEmpresa"
	       			, MovimientoComprobante."IdComprobante"
	       			, MovimientoComprobante."NemotecnicoSubyacente"
	       		ORDER BY
	       			MovimientoComprobante."IdMovimientoComprobante" DESC
	        ) AS "Ranking"
	    	, MovimientoComprobante."IdMovimientoComprobante" AS "IdMovimientoComprobante"
	    	, MovimientoComprobante."IdEmpresa"
			, MovimientoComprobante."IdComprobante"
			, MovimientoComprobante."NemotecnicoSubyacente"
		FROM
			"BSCL.Facturacion.BD::FAC.MovimientoComprobante" MovimientoComprobante
	)
	, CteCliente AS (
		SELECT
			Cliente."IdCliente" AS "IdCliente"
		FROM
			"BSCL.SocioNegocio.BD::PER.Persona" Persona
			INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DocumentoIdentidad ON (DocumentoIdentidad."IdDocumentoIdentidad" = Persona."IdDocumentoIdentidad")
			INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" Cliente ON (Persona."IdPersona" = Cliente."IdPersona")
			INNER JOIN "BSCL.Plataforma.BD::COM.CategoriaInversionista" CategoriaInv ON (CategoriaInv."IdCategoriaInversionista" = Cliente."IdCategoriaInversionista" AND CategoriaInv."IdEmpresa" = Cliente."IdEmpresa")
		WHERE
				Persona."Borrado" = 'N'
			AND DocumentoIdentidad."Borrado" = 'N'
			AND Cliente."Borrado" = 'N'
	)
	, CteCartera AS (
		SELECT
			Cartera."IdCartera" AS "IdCartera"
			, Filtro."TablaOrigen" AS "TablaOrigen"
		FROM
			"BSCL.Custodia.BD::CUS.Cartera" Cartera
			INNER JOIN "BSCL.Plataforma.BD::LIM.ConsultarCustodiaPortafolioCuentaPropia_FiltroCartera" Filtro ON
			(
					Filtro."IdTipoInstrumento" = Cartera."IdTipoInstrumento"
				AND	(Filtro."IdTipoOperacion" IS NULL OR Filtro."IdTipoOperacion" = Cartera."IdTipoOperacion")
			)
		WHERE
				Cartera."IdEmpresa" = :IdEmpresa
			AND Cartera."IdEstado" = 'VIG'
			AND
			(
					(:TipoFechaConsulta = 'LIQ' AND Cartera."INGFechaLiquidacion" <= :FechaConsulta)
				OR	(:TipoFechaConsulta = 'ADQ' AND Cartera."INGFechaOperacion" <= :FechaConsulta)
			)
	)
	, CteCustodiaTotal AS (
		SELECT
			Filtro."TablaOrigen" AS "TablaOrigen"
			, Cartera."IdCliente" AS "IdCliente"
			, Cartera."IdPortafolio" AS "IdPortafolio"
			, Cartera."IdInstrumento" AS "IdInstrumento"
			, Cartera."IdTipoInstrumento" AS "IdTipoInstrumento"
			, Cartera."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
			, Cartera."IdMonedaInstrumento" AS "IdMonedaInstrumento"
			, COALESCE(Cartera."IdEmisor", Instrumento."IdEmisor", '') AS "IdEmisor" 
			, CASE WHEN Cartera."IdClasificacionTipoInstrumento" = 'IF' THEN Cartera."NemoSVS" ELSE Cartera."Nemotecnico" END AS "Nemotecnico"
			, CASE WHEN IFNULL(Cartera."IdMonedaInstrumento", '') = 'UF' THEN 'CLP' ELSE Cartera."IdMonedaInstrumento" END AS "EsPagaderoEn"
			, 'N' AS "EsNominal"
			, '' AS "IdTipoOperacion"
			, '' AS "AccionCustodia"
			, CAST(IFNULL(CASE WHEN Cartera."IdClasificacionTipoInstrumento" = 'IF' THEN Cartera."INGValorRescate" - COALESCE(ReservaTotal."IngValorRescate", 0) ELSE Cartera."INGCantidad" - COALESCE(ReservaTotal."IngCantidad", 0) END, 0) AS decimal(26, 8)) AS "Cantidad"
			, 0.0 AS "CantidadNominal"
			, Cartera."INGMontoCompraActualMonedaInstrumento" AS "MontoCompra"
			, '' AS "EsVentaDescubierta"
		FROM
			"BSCL.Custodia.BD::CUS.Cartera" Cartera
			INNER JOIN CteCartera Filtro ON (Filtro."IdCartera" = Cartera."IdCartera" AND Filtro."TablaOrigen" = 'CteCustodiaTotal')
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" Instrumento ON (Instrumento."IdInstrumento" = Cartera."IdInstrumento")
			LEFT JOIN CteUtilityViewReservaTotal ReservaTotal ON (ReservaTotal."FolioCarteraOrigen" = Cartera."FolioCartera")
		WHERE
				Cartera."IdCliente" = CASE WHEN :IdCliente > 0 THEN :IdCliente ELSE Cartera."IdCliente" END
			AND Cartera."IdPortafolio" = CASE WHEN :IdPortafolio > 0 THEN :IdPortafolio ELSE Cartera."IdPortafolio" END
			AND Cartera."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(:IdClasificacionTipoInstrumento, '') <> '' THEN :IdClasificacionTipoInstrumento ELSE Cartera."IdClasificacionTipoInstrumento" END
			AND Cartera."IdTipoInstrumento" = CASE WHEN IFNULL(:IdTipoInstrumento, '') <> '' THEN :IdTipoInstrumento ELSE Cartera."IdTipoInstrumento" END
			AND Cartera."IdInstrumento" = CASE WHEN IFNULL(:IdInstrumento, 0) > 0 THEN :IdInstrumento ELSE Cartera."IdInstrumento" END
	)
	, CtePactoCompraVentaTotal AS (
		SELECT
			Filtro."TablaOrigen" AS "TablaOrigen"
			, CASE WHEN :IdCliente > 0 AND :IdCliente = :IdClienteCuentaPropia THEN Cartera."IdClienteRelacionado" ELSE Cartera."IdCliente" END AS "IdCliente"
			, CASE WHEN :IdCliente > 0 AND :IdCliente = :IdClienteCuentaPropia THEN Cartera."IdPortafolioRelacionado" ELSE Cartera."IdPortafolio" END AS "IdPortafolio"
			, CarteraSubyacente."IdInstrumento" AS "IdInstrumento"
			, CarteraSubyacente."IdTipoInstrumento" AS "IdTipoInstrumento"
			, CarteraSubyacente."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
			, CarteraSubyacente."IdMonedaInstrumento" AS "IdMonedaInstrumento"
		    , CarteraSubyacente."IdEmisor" AS "IdEmisor"
			, CASE WHEN CarteraSubyacente."IdClasificacionTipoInstrumento" = 'IF' THEN CarteraSubyacente."NemoSVS" ELSE CarteraSubyacente."Nemotecnico" END AS "Nemotecnico"
			, CASE WHEN IFNULL(Cartera."IdMonedaInstrumento", '') = 'UF' THEN 'CLP' ELSE Cartera."IdMonedaInstrumento" END AS "EsPagaderoEn"
			, CASE WHEN ReservaSubyacente."FolioCarteraOrigen" = 0 THEN 'S' ELSE 'N' END AS "EsNominal"
			, '' AS "IdTipoOperacion"
			, '' AS "AccionCustodia"
			, CASE
				WHEN ReservaSubyacente."FolioCarteraOrigen" != 0 AND CarteraSubyacente."IdClasificacionTipoInstrumento" = 'IF' THEN CarteraSubyacente."INGValorRescate"
				WHEN ReservaSubyacente."FolioCarteraOrigen" != 0 THEN CarteraSubyacente."INGCantidad"
				ELSE 0
			END AS "Cantidad"
			, CASE
				WHEN ReservaSubyacente."FolioCarteraOrigen" = 0 AND CarteraSubyacente."IdClasificacionTipoInstrumento" = 'IF' THEN CarteraSubyacente."INGValorRescate"
				WHEN ReservaSubyacente."FolioCarteraOrigen" = 0 THEN CarteraSubyacente."INGCantidad"
				ELSE 0
			END AS "CantidadNominal"
			, CarteraSubyacente."INGMontoCompraActualMonedaInstrumento" AS "MontoCompra"
			, '' AS "EsVentaDescubierta"
		FROM
			"BSCL.Custodia.BD::CUS.Cartera" Cartera
			INNER JOIN CteCartera Filtro ON (Filtro."IdCartera" = Cartera."IdCartera" AND (Filtro."TablaOrigen" = 'CtePactoCompraTotal' OR Filtro."TablaOrigen" = 'CtePactoVentaTotal'))
			LEFT JOIN CteUtilityViewCarteraSubyacenteBasica CarteraSubyacente ON (CarteraSubyacente."FolioCarteraRelacionada" = Cartera."FolioCartera")
			LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" ReservaSubyacente ON (ReservaSubyacente."FolioSubyacente" = CarteraSubyacente."FolioSubyacente")
		WHERE
			    (
					CASE WHEN :IdCliente > 0 AND :IdCliente = :IdClienteCuentaPropia THEN Cartera."IdClienteRelacionado" ELSE Cartera."IdCliente" END
					=
					CASE WHEN :IdCliente > 0 THEN :IdCliente ELSE Cartera."IdCliente" END
				)
			AND
			    (
					CASE WHEN :IdCliente > 0 AND :IdCliente = :IdClienteCuentaPropia THEN Cartera."IdPortafolioRelacionado" END
					=
					CASE WHEN :IdPortafolio > 0 THEN :IdPortafolio ELSE Cartera."IdPortafolioRelacionado" END
				)
			AND	CarteraSubyacente."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(:IdClasificacionTipoInstrumento, '') <> '' THEN :IdClasificacionTipoInstrumento ELSE CarteraSubyacente."IdClasificacionTipoInstrumento" END
			AND	CarteraSubyacente."IdTipoInstrumento" = CASE WHEN IFNULL(:IdTipoInstrumento, '') <> '' THEN :IdTipoInstrumento ELSE CarteraSubyacente."IdTipoInstrumento" END
			AND	CarteraSubyacente."IdInstrumento" = CASE WHEN :IdInstrumento > 0 THEN :IdInstrumento ELSE CarteraSubyacente."IdInstrumento" END
	)
	, CtePrestamosVentaCortaTotal AS (
		SELECT
			Filtro."TablaOrigen" AS "TablaOrigen"
			, Cartera."IdCliente" AS "IdCliente"
			, Cartera."IdPortafolio" AS "IdPortafolio"
			, Subyacente."IdInstrumento" AS "IdInstrumento"
			, Subyacente."IdTipoInstrumento" AS "IdTipoInstrumento"
			, Subyacente."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
			, COALESCE(Subyacente."IdMonedaInstrumento", '') AS "IdMonedaInstrumento"
		    , Subyacente."IdEmisor" AS "IdEmisor"
			, CASE WHEN Subyacente."IdClasificacionTipoInstrumento" = 'IF' THEN Subyacente."NemoSVS" ELSE Subyacente."Nemotecnico" END AS "Nemotecnico"
			, '' AS "EsPagaderoEn"
			, 'N' AS "EsNominal"
			, '' AS "IdTipoOperacion"
			, '' AS "AccionCustodia"
			, CAST
			(
			    IFNULL
				(
			        CASE WHEN Subyacente."IdClasificacionTipoInstrumento" = 'IF' THEN Cartera."INGValorRescate" ELSE Cartera."INGCantidad" END
			        , 0
				) AS DECIMAL(26, 8)
			) AS "Cantidad"
			, 0.0 AS "CantidadNominal"
			, Cartera."INGMontoCompraActualMonedaInstrumento" AS "MontoCompra"
			, '' AS "EsVentaDescubierta"
		FROM
			"BSCL.Custodia.BD::CUS.Cartera" Cartera
			INNER JOIN CteCartera Filtro ON (Filtro."IdCartera" = Cartera."IdCartera" AND Filtro."TablaOrigen" = 'CtePrestamosVentaCortaTotal')
			INNER JOIN CteUtilityViewUltimoMovimientoSubyacente UltimoMovimientoSubyacente ON (Cartera."FolioCartera" = UltimoMovimientoSubyacente."FolioCarteraRelacionada")
			INNER JOIN CteUtilityViewCarteraSubyacenteBasica Subyacente ON (Subyacente."IdSubyacente" = UltimoMovimientoSubyacente."IdSubyacente")
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" InstrumentoSubyacente ON (InstrumentoSubyacente."IdInstrumento" = Subyacente."IdInstrumento")
			LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TipoIndicadorCC ON (TipoIndicadorCC."CodigoTipoIndicador" = 'CC')
			LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IndicadorCC ON (IndicadorCC."IdTipoIndicador" = TipoIndicadorCC."IdTipoIndicador" AND IndicadorCC."IdTipoEntidad" = 'CLIENTE' AND IndicadorCC."IdEntidad" = TO_VARCHAR(Cartera."IdCliente"))
		WHERE
				Cartera."IdCliente" = CASE WHEN :IdCliente > 0 THEN :IdCliente ELSE Cartera."IdCliente" END
			AND	Cartera."IdPortafolio" = CASE WHEN :IdPortafolio > 0 THEN :IdPortafolio ELSE Cartera."IdPortafolio" END
		  	AND Cartera."Nemotecnico" NOT IN ('ADMINISTRATIVO', 'FINES GENERALES')
			AND	Subyacente."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(:IdClasificacionTipoInstrumento,'') <> '' THEN :IdClasificacionTipoInstrumento ELSE Subyacente."IdClasificacionTipoInstrumento" END
			AND	Subyacente."IdTipoInstrumento" = CASE WHEN IFNULL(:IdTipoInstrumento,'') <> '' THEN :IdTipoInstrumento ELSE Subyacente."IdTipoInstrumento" END
			AND	Subyacente."IdInstrumento" = CASE WHEN IFNULL(:IdInstrumento, 0) > 0 THEN :IdInstrumento ELSE Subyacente."IdInstrumento" END
			AND IndicadorCC."ValorIndicador" = 'S'
	)
	, CteEmprestitoTotal AS (
		SELECT
			CASE WHEN IFNULL(IndicadorCC."ValorIndicador", 'N') = 'S' THEN 'CteEmprestitoTotal' ELSE 'CteEmprestitoExternoTotal' END AS "TablaOrigen"
			, CarteraEmprestito."IdCliente" AS "IdCliente"
			, CarteraEmprestito."IdPortafolio" AS "IdPortafolio"
			, UltimoSubyacente."IdInstrumento" AS "IdInstrumento"
			, UltimoSubyacente."IdTipoInstrumento" AS "IdTipoInstrumento"
			, UltimoSubyacente."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
			, UltimoSubyacente."IdMonedaInstrumento" AS "IdMonedaInstrumento"
			, UltimoSubyacente."IdEmisor" AS "IdEmisor"
			, CASE WHEN UltimoSubyacente."IdClasificacionTipoInstrumento" = 'IF' THEN UltimoSubyacente."NemoSVS" ELSE UltimoSubyacente."Nemotecnico" END AS "Nemotecnico"
			, '' AS "EsPagaderoEn"
			, 'N' AS "EsNominal"
			, '' AS "IdTipoOperacion"
			, '' AS "AccionCustodia"
			, CAST
			(
				IFNULL
				(
					CASE WHEN UltimoSubyacente."IdClasificacionTipoInstrumento" = 'IF' THEN CarteraEmprestito."INGValorRescate" ELSE CarteraEmprestito."INGCantidad" END
					, 0
				) AS DECIMAL(26, 8)
			) AS "Cantidad"
			, 0 AS "CantidadNominal"
			, CarteraEmprestito."INGMontoCompraActualMonedaInstrumento" AS "MontoCompra"
			, '' AS "EsVentaDescubierta"
		FROM
			"BSCL.Custodia.BD::CUS.Cartera" CarteraEmprestito
			INNER JOIN CteCartera Filtro ON (Filtro."IdCartera" = CarteraEmprestito."IdCartera" AND (Filtro."TablaOrigen" = 'CteEmprestitoTotal' OR Filtro."TablaOrigen" = 'CteEmprestitoExternoTotal'))
			INNER JOIN CteUtilityViewUltimoMovimientoSubyacente UltimoMovSubyacente ON (UltimoMovSubyacente."FolioCarteraRelacionada" = CarteraEmprestito."FolioCartera")
			INNER JOIN CteUtilityViewUltimoMovimientoCartera UltimoMovCartera ON (UltimoMovCartera."IdEmpresa" = CarteraEmprestito."IdEmpresa" AND UltimoMovCartera."FolioCartera" = CarteraEmprestito."FolioCarteraRelacionada")
			INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" UltimaCartera ON (UltimaCartera."IdCartera" = UltimoMovCartera."IdCartera")
			LEFT JOIN CteUtilityViewCarteraSubyacenteBasica UltimoSubyacente ON (UltimoSubyacente."IdSubyacente" = UltimoMovSubyacente."IdSubyacente")
			LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CarteraOrigen ON (CarteraOrigen."IdCartera" = UltimoSubyacente."IdCarteraRetirada")
			LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TipoIndicadorCC ON (TipoIndicadorCC."CodigoTipoIndicador" = 'CC')
			LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IndicadorCC ON (IndicadorCC."IdTipoIndicador" = TipoIndicadorCC."IdTipoIndicador" AND IndicadorCC."IdTipoEntidad" = 'CLIENTE' AND IndicadorCC."IdEntidad" = TO_VARCHAR(CarteraEmprestito."IdCliente"))
		WHERE
				CarteraEmprestito."IdCliente" = CASE WHEN :IdCliente > 0 THEN :IdCliente ELSE CarteraEmprestito."IdCliente" END
			AND	CarteraEmprestito."IdPortafolio" = CASE WHEN :IdPortafolio > 0 THEN :IdPortafolio ELSE CarteraEmprestito."IdPortafolio" END
			AND	UltimoSubyacente."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(:IdClasificacionTipoInstrumento, '') <> '' THEN :IdClasificacionTipoInstrumento ELSE UltimoSubyacente."IdClasificacionTipoInstrumento" END
			AND	UltimoSubyacente."IdTipoInstrumento" = CASE WHEN IFNULL(:IdTipoInstrumento, '') <> '' THEN :IdTipoInstrumento ELSE UltimoSubyacente."IdTipoInstrumento" END
			AND	UltimoSubyacente."IdInstrumento" = CASE WHEN IFNULL(:IdInstrumento, 0) > 0 THEN :IdInstrumento ELSE UltimoSubyacente."IdInstrumento" END
	)
	, CteSimultaneaCompraVentaTotal AS (
		SELECT
			Filtro."TablaOrigen" AS "TablaOrigen"
			, Cartera."IdCliente" AS "IdCliente"
			, Cartera."IdPortafolio" AS "IdPortafolio"
			, Subyacente."IdInstrumento" AS "IdInstrumento"
			, Subyacente."IdTipoInstrumento" AS "IdTipoInstrumento"
			, Subyacente."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
			, Subyacente."IdMonedaInstrumento" AS "IdMonedaInstrumento"
			, Subyacente."IdEmisor" AS "IdEmisor"
			, CASE WHEN Subyacente."IdClasificacionTipoInstrumento" = 'IF' THEN Subyacente."NemoSVS" ELSE Subyacente."Nemotecnico" END AS "Nemotecnico"
			, Subyacente."IdMonedaEmision" AS "EsPagaderoEn"
			, 'N' AS "EsNominal"
			, Cartera."IdTipoOperacion" AS "IdTipoOperacion"
			, '' AS "AccionCustodia"
			, CAST(IFNULL(CASE WHEN Subyacente."IdClasificacionTipoInstrumento" = 'IF' THEN Cartera."INGValorRescate" ELSE Cartera."INGCantidad" END, 0) AS decimal(26, 8)) AS "Cantidad"
			, 0 AS "CantidadNominal"
			, Cartera."INGMontoCompraActualMonedaInstrumento" AS "MontoCompra"
			, '' AS "EsVentaDescubierta"
		FROM
			"BSCL.Custodia.BD::CUS.Cartera" Cartera
			INNER JOIN CteCartera Filtro ON (Filtro."IdCartera" = Cartera."IdCartera" AND (Filtro."TablaOrigen" = 'CteSimultaneaCompraVentaTotal' OR Filtro."TablaOrigen" = 'CteReporteCompraVentaTotal'))
			INNER JOIN CteUtilityViewUltimoMovimientoSubyacente UltimoSubyacente ON (UltimoSubyacente."FolioCarteraRelacionada" = Cartera."FolioCartera")
			INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" Subyacente ON (Subyacente."IdSubyacente" = UltimoSubyacente."IdSubyacente")
			INNER JOIN CteCliente Cliente ON (Cliente."IdCliente" = Cartera."IdCliente")
			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" Portafolio ON (Portafolio."IdPortafolio" = Cartera."IdPortafolio")
			INNER JOIN CteMovimientoComprobanteUnico MovimientoComprobanteUnico ON (MovimientoComprobanteUnico."IdEmpresa" = Cartera."IdEmpresa" AND MovimientoComprobanteUnico."IdComprobante" = Cartera."FolioComprobante" AND MovimientoComprobanteUnico."NemotecnicoSubyacente" = Subyacente."Nemotecnico")
		WHERE
				Cartera."IdCliente" = CASE WHEN :IdCliente > 0 THEN :IdCliente ELSE Cartera."IdCliente" END
			AND Cartera."IdPortafolio" = CASE WHEN :IdPortafolio > 0 THEN :IdPortafolio ELSE Cartera."IdPortafolio" END
			AND Subyacente."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(:IdClasificacionTipoInstrumento, '') <> '' THEN :IdClasificacionTipoInstrumento ELSE Subyacente."IdClasificacionTipoInstrumento" END
			AND Subyacente."IdTipoInstrumento" = CASE WHEN IFNULL(:IdTipoInstrumento, '') <> '' THEN :IdTipoInstrumento ELSE Subyacente."IdTipoInstrumento" END
			AND Subyacente."IdInstrumento" = CASE WHEN IFNULL(:IdInstrumento, 0) > 0 THEN :IdInstrumento ELSE Subyacente."IdInstrumento" END
			AND MovimientoComprobanteUnico."Ranking" = 1
	)
SELECT
	"TablaOrigen", "IdCliente", "IdPortafolio", "IdInstrumento", "IdTipoInstrumento"
	, "IdClasificacionTipoInstrumento", "IdMonedaInstrumento", "IdEmisor", "Nemotecnico", "EsPagaderoEn"
	, "EsNominal", "IdTipoOperacion", "AccionCustodia", "Cantidad", "CantidadNominal", "MontoCompra"
	, "EsVentaDescubierta"
FROM
(
	SELECT
		"TablaOrigen", "IdCliente", "IdPortafolio", "IdInstrumento", "IdTipoInstrumento"
		, "IdClasificacionTipoInstrumento", "IdMonedaInstrumento", "IdEmisor", "Nemotecnico", "EsPagaderoEn"
		, "EsNominal", "IdTipoOperacion", "AccionCustodia", "Cantidad", "CantidadNominal", "MontoCompra"
		, "EsVentaDescubierta"
	FROM CteCustodiaTotal	
	UNION ALL
	SELECT
		"TablaOrigen", "IdCliente", "IdPortafolio", "IdInstrumento", "IdTipoInstrumento"
		, "IdClasificacionTipoInstrumento", "IdMonedaInstrumento", "IdEmisor", "Nemotecnico", "EsPagaderoEn"
		, "EsNominal", "IdTipoOperacion", "AccionCustodia", "Cantidad", "CantidadNominal", "MontoCompra"
		, "EsVentaDescubierta"
	FROM CtePactoCompraVentaTotal
	UNION ALL
	SELECT
		"TablaOrigen", "IdCliente", "IdPortafolio", "IdInstrumento", "IdTipoInstrumento"
		, "IdClasificacionTipoInstrumento", "IdMonedaInstrumento", "IdEmisor", "Nemotecnico", "EsPagaderoEn"
		, "EsNominal", "IdTipoOperacion", "AccionCustodia", "Cantidad", "CantidadNominal", "MontoCompra"
		, "EsVentaDescubierta"
	FROM CtePrestamosVentaCortaTotal
	UNION ALL
	SELECT
		"TablaOrigen", "IdCliente", "IdPortafolio", "IdInstrumento", "IdTipoInstrumento"
		, "IdClasificacionTipoInstrumento", "IdMonedaInstrumento", "IdEmisor", "Nemotecnico", "EsPagaderoEn"
		, "EsNominal", "IdTipoOperacion", "AccionCustodia", "Cantidad", "CantidadNominal", "MontoCompra"
		, "EsVentaDescubierta"
	FROM CteEmprestitoTotal
	UNION ALL
	SELECT
		"TablaOrigen", "IdCliente", "IdPortafolio", "IdInstrumento", "IdTipoInstrumento"
		, "IdClasificacionTipoInstrumento", "IdMonedaInstrumento", "IdEmisor", "Nemotecnico", "EsPagaderoEn"
		, "EsNominal", "IdTipoOperacion", "AccionCustodia", "Cantidad", "CantidadNominal", "MontoCompra"
		, "EsVentaDescubierta"
	FROM CteSimultaneaCompraVentaTotal
);
