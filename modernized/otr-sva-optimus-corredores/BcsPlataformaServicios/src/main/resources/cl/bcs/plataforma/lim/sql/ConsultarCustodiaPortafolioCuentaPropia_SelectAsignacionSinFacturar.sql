WITH
	CteAsignacionSinFacturarTotal AS (
		SELECT
			'CteAsignacionSinFacturarTotal' AS "TablaOrigen"
			, Portafolio."IdCliente" AS "IdCliente"
			, Orden."IdPortafolioCliente" AS "IdPortafolio"
			, CASE WHEN TipoInstrumento."IdTipoInstrumento" = 'SIM' THEN Transaccion."IdInstrumentoSubyacente" ELSE Transaccion."IdInstrumento" END AS "IdInstrumento"
			, TipoInstrumento."IdTipoInstrumento" AS "IdTipoInstrumento"
			, TipoInstrumento."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
			, Instrumento."IdMoneda" AS "IdMonedaInstrumento"
			, Transaccion."Tasa" AS "Tasa"
			, CASE WHEN TipoInstrumento."IdTipoInstrumento" = 'SIM' THEN InstrumentoSubyacente."Nemotecnico" ELSE Transaccion."Nemotecnico" END AS "Nemotecnico"
			, '' AS "EsPagaderoEn"
			, '' AS "EsNominal"
			, Transaccion."IdTipoOperacion" AS "IdTipoOperacion"
			, '' AS "AccionCustodia"
			, CASE TipoInstrumento."IdClasificacionTipoInstrumento" WHEN 'IF' THEN Asignacion."ValorRescateAsignado"  ELSE Asignacion."CantidadAsignada" END AS "Cantidad"
			, 0.0 AS "CantidadNominal"
			, '' AS "EsVentaDescubierta"
			, Asignacion."MontoAsignado" AS "MontoCompra"
			, Orden."IdEmpresa" AS "IdEmpresa"
			, Asignacion."FechaAsignacion" AS "FechaAsignacion"
			, Asignacion."IdEstadoAsignacion" AS "IdEstadoAsignacion"
			, MovimientoComprobante."IdEstadoFacturacion" AS "IdEstadoFacturacion"
		FROM
			"BSCL.Orden.BD::ORD.Asignacion" AS Asignacion
			LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" AS Transaccion ON (Transaccion."IdTransaccion" = Asignacion."IdTransaccion")
			LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MovimientoComprobante on (MovimientoComprobante."IdMovimientoComprobante" = Asignacion."IdMovimientoComprobante") 
			LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS Orden ON (Orden."IdOrden" = Asignacion."IdOrden")
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS InstrumentoSubyacente ON (InstrumentoSubyacente."IdInstrumento" = Transaccion."IdInstrumentoSubyacente")
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento ON (Instrumento."IdInstrumento" = Transaccion."IdInstrumento")
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento ON (TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento")
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON (Portafolio."IdPortafolio" = Orden."IdPortafolioCliente")
		WHERE
				TipoInstrumento."IdClasificacionTipoInstrumento" != 'RE'
			AND Asignacion."IdEstadoAsignacion" NOT IN ('ANU', 'REF', 'PDA')
			AND MovimientoComprobante."IdEstadoFacturacion" = 'ING'
	)
SELECT
	'CteAsignacionSinFacturarTotal' AS "TablaOrigen"
	, Asignacion."IdCliente" AS "IdCliente"
	, Asignacion."IdPortafolio" AS "IdPortafolio"
	, Asignacion."IdInstrumento" AS "IdInstrumento"
	, Asignacion."IdTipoInstrumento" AS "IdTipoInstrumento"
	, Asignacion."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
	, Asignacion."IdMonedaInstrumento" AS "IdMonedaInstrumento"
	, Asignacion."Tasa" AS "Tasa"
	, Asignacion."Nemotecnico" AS "Nemotecnico"
	, Asignacion."EsPagaderoEn" AS "EsPagaderoEn"
	, Asignacion."EsNominal" AS "EsNominal"
	, Asignacion."IdTipoOperacion" AS "IdTipoOperacion"
	, Asignacion."AccionCustodia" AS "AccionCustodia"
	, Asignacion."Cantidad" AS "Cantidad"
	, Asignacion."CantidadNominal" AS "CantidadNominal"
	, Asignacion."EsVentaDescubierta" AS "EsVentaDescubierta"
	, Asignacion."MontoCompra" AS "MontoCompra"	
FROM
	CteAsignacionSinFacturarTotal Asignacion
WHERE
		Asignacion."IdEmpresa" = :IdEmpresa
	AND (Asignacion."IdCliente" = :IdCliente)
	AND (:IdClasificacionTipoInstrumento IS NULL OR Asignacion."IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento)
	AND (:FechaConsulta IS NULL OR Asignacion."FechaAsignacion" < :FechaConsulta);
