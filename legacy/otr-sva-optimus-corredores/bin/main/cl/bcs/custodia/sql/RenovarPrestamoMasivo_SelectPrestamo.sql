WITH
	CtePrestamoSolicitado AS (
		SELECT
			Prestamo."IdCartera" AS "IdCartera"
			, Prestamo."FolioCartera" AS "FolioCartera"
		FROM
			"BSCL.Custodia.BD::CUS.Cartera" Prestamo
		WHERE
			Prestamo."IdTipoInstrumento" IN ('PRT', 'PRF')
	)
	, CtePrimeraCartera AS (
		SELECT
			Cartera."FolioCartera" AS "FolioCartera"
			, MIN(Cartera."IdCartera") AS "IdCartera"
		FROM
			"BSCL.Custodia.BD::CUS.Cartera" Cartera
		WHERE
			Cartera."IdTipoInstrumento" IN ('PRT','PRF')
		GROUP BY
			Cartera."FolioCartera"
	)
	, CteCalculosRenovacionesPrestamo AS (
		SELECT Cartera."FolioCarteraRelacionada" AS "FolioCarteraPrestamoOriginal"
			 , SUM(COALESCE(Cartera."PlazoEnDiasOperacion", 0) - Cartera."PlazoResidual") AS "TotalDiasRenovados"
		FROM (
			SELECT
				Renovacion."FolioCarteraRelacionada"
				, COALESCE(Renovacion."PlazoEnDiasOperacion", 0) AS "PlazoEnDiasOperacion"
				, CASE WHEN "INGCantidad" = "EGRCantidad" AND "EGRFechaOperacion" < "FechaVencimientoOperacion" 
					THEN "BSCL.Custodia.BD.Utilitarios.PRT::CalcularPlazo"(Renovacion."EGRFechaOperacion", Renovacion."FechaVencimientoOperacion", tp."EsDiaHabilPlazoPrestamo", 'T0', (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" conf WHERE conf."CodigoParametro" = 'PLA.PaisSistema' AND conf."IdEmpresa" = Renovacion."IdEmpresa"))
					ELSE 0 
				  END AS "PlazoResidual"
			FROM
				"BSCL.Custodia.BD::CUS.Cartera" Renovacion
			INNER JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo" tp ON tp."IdTipoPrestamo" = Renovacion."IdTipoPrestamo"
			WHERE
					Renovacion."IdTipoInstrumento" IN ('PRT','PRF')
				AND	Renovacion."IdEstado" = 'RNV'
				AND Renovacion."FolioCarteraRelacionada" > 0
			
		) Cartera
		GROUP BY
			Cartera."FolioCarteraRelacionada"
	)
	, CtePosicionSubyacente AS (
		SELECT
			Subyacente."IdSubyacente" AS "IdSubyacente"
			, CASE Subyacente."IdClasificacionTipoInstrumento" WHEN 'IF' THEN "ValorRescate" ELSE "Cantidad" END AS "Posicion"
			, CASE Subyacente."IdClasificacionTipoInstrumento" WHEN 'IF' THEN "INGValorRescate" ELSE "INGCantidad" END AS "INGPosicion"
			, CASE Subyacente."IdClasificacionTipoInstrumento" WHEN 'IF' THEN "EGRValorRescate" ELSE "EGRCantidad" END AS "EGRPosicion"
		FROM
			"BSCL.Custodia.BD::CUS.Subyacente" Subyacente
	)
	, CteIndicadoresCliente AS (
		SELECT
			IndicadoresCliente."IdCliente" AS "IdCliente"
			, COALESCE(MAX(CASE IndicadoresCliente."CodigoTipoIndicador" WHEN 'CC' THEN IndicadoresCliente."ValorIndicador" END), 'N') AS "TieneContratoCustodia"
		FROM
			"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IndicadoresCliente
		GROUP BY
			IndicadoresCliente."IdCliente"
	)
SELECT
	Prestamo."IdCartera" AS "IdCartera"
	, Prestamo."FolioCartera" AS "FolioCartera"
	, CASE PrimeraCarteraPrestamo."FolioCarteraRelacionada"
	 	WHEN 0 THEN PrimeraCarteraPrestamo."FolioCartera"
	 	ELSE PrimeraCarteraPrestamo."FolioCarteraRelacionada"
	 END  AS "FolioCarteraPrestamoOriginal" -- Si es primera renovacion corresponde a FolioCartera de prestamo solicitado, en caso contrario corresponde a FolioCartera del primer prestamo (guardado en FolioCarteraRelacionada)
	, Prestamo."IdEstado" AS "IdEstado"
	, Prestamo."FechaVencimientoOperacion" AS "FechaVencimiento"
	, Prestamo."FechaVencimientoOperacion" AS "FechaVencimiento"
	, Prestamo."IdCliente" AS "IdCliente"
	, Prestamo."IdPortafolio" AS "IdPortafolio"
	, Prestamo."IdBolsa" AS "IdBolsa"
	, Prestamo."IdInstrumento" AS "IdInstrumentoPrestamo"
	, Subyacente."IdInstrumento" AS "IdInstrumento"
	, Prestamo."Cantidad" AS "Cantidad"
	, Prestamo."PrecioCompra" AS "Precio"
	, Prestamo."UsaGarantiaEspecifica" AS "UsaGarantiaEspecifica"
	, Prestamo."FolioExternoReferencia" AS "FolioExternoReferencia"
	, Prestamo."CobraPrimaAlInicio" AS "CobraPrimaAlInicio"
	, Prestamo."IdAgente" AS "IdAgente"
	, Prestamo."IdSucursal" AS "IdSucursal"
	, MAP(IndicadoresCliente."TieneContratoCustodia", 'N', 'S', 'S', 'N', 'N') AS "EsNominal"
	, Prestamo."EsFungible" AS "EsFungible"
	, Prestamo."TipoCustodia" AS "TipoCustodia"
	, Prestamo."FolioSistemaExterno" AS "FolioSistemaExterno"
	, Subyacente."Nemotecnico" AS "Nemotecnico"
	, Subyacente."IdClasificacionTipoInstrumento" AS "IdMercado"
	, TipoPrestamo."EsRenovable" AS "EsRenovable"
	, TipoPrestamo."MaximoDiasPrestamo" AS "MaximoDiasPrestamo"
	, TipoPrestamo."MinimoDiasPrestamo" AS "MinimoDiasPrestamo"
	, (Prestamo."PlazoEnDiasOperacion" - (CASE WHEN Prestamo."INGCantidad" = Prestamo."EGRCantidad" AND Prestamo."EGRFechaOperacion" < Prestamo."FechaVencimientoOperacion" 
											THEN "BSCL.Custodia.BD.Utilitarios.PRT::CalcularPlazo"(Prestamo."EGRFechaOperacion", Prestamo."FechaVencimientoOperacion", TipoPrestamo."EsDiaHabilPlazoPrestamo", 'T0', (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" conf WHERE conf."CodigoParametro" = 'PLA.PaisSistema' AND conf."IdEmpresa" = Prestamo."IdEmpresa")) 
											ELSE 0 END)) + COALESCE(CalculosRenovacionesPrevias."TotalDiasRenovados", 0) AS "TotalDiasRenovados"
	, PosicionSubyacente."INGPosicion" AS "INGPosicion"
	, PosicionSubyacente."EGRPosicion" AS "EGRPosicion"
FROM
	CtePrestamoSolicitado FiltroPrestamo
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" Prestamo ON (Prestamo."IdCartera" = FiltroPrestamo."IdCartera")
	INNER JOIN "BSCL.Custodia.BD::PRT.TipoPrestamo" TipoPrestamo ON (TipoPrestamo."IdInstrumentoPrestamo" = Prestamo."IdInstrumento")
	INNER JOIN CtePrimeraCartera FiltroPrimeraCarteraPrestamo ON (FiltroPrimeraCarteraPrestamo."FolioCartera" = Prestamo."FolioCartera")
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" PrimeraCarteraPrestamo ON (PrimeraCarteraPrestamo."IdCartera" = FiltroPrimeraCarteraPrestamo."IdCartera")
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" FiltroSubyacente ON (FiltroSubyacente."FolioCarteraRelacionada" = PrimeraCarteraPrestamo."FolioCartera")
	INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" Subyacente ON (Subyacente."IdSubyacente" = FiltroSubyacente."IdSubyacente")
	INNER JOIN CtePosicionSubyacente PosicionSubyacente ON (PosicionSubyacente."IdSubyacente" = Subyacente."IdSubyacente")
	LEFT JOIN CteCalculosRenovacionesPrestamo CalculosRenovacionesPrevias ON (CalculosRenovacionesPrevias."FolioCarteraPrestamoOriginal" = PrimeraCarteraPrestamo."FolioCarteraRelacionada")
	LEFT JOIN CteIndicadoresCliente IndicadoresCliente ON (IndicadoresCliente."IdCliente" = Prestamo."IdCliente")
WHERE
	Prestamo."IdCartera" IN (:IdCartera)
