SELECT
	asig."IdMovimientoComprobante" ,
	mov."EstadoFacturacion",
	mov."NombreEstadoFacturacion",
	asig."IdAsignacion",
	asig."FolioTransaccion",
	asig."FechaAsignacion",
	asig."IdEstadoAsignacion",
	asig."EstadoAsignacion",
	asig."AccionOrden",
	asig."NombreTipoOperacion",
	asig."InstrumentoUI",
	asig."IdBolsa",
	asig."NombreBolsa",
	asig."IdTipoInstrumento",
	asig."NombreTipoInstrumento",
	asig."IdClasificacionTipoInstrumento",
	asig."IdCliente",
	asig."DocumentoIdentidad",
	asig."NombreCompleto" AS "NombreCompleto",
	CASE
		WHEN asig."IdEstadoAsignacion" = 'ANU' THEN 'UI_ANULADA'
		WHEN asig."IdEstadoAsignacion" <> 'EAF' THEN 'UI_FALTA_ENVIAR_FACTURAR'
		WHEN mov."IdMovimientoComprobante" IS NOT NULL AND mov."EstadoFacturacion" <> 'FAC' THEN 'UI_FALTA_GENERAR_COMPROBANTE'
		ELSE ''
	END AS "Descripcion"
FROM "BSCL.Orden.BD.Vistas::ConsultarAsignacion" asig
	LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" mov 
		on asig."IdMovimientoComprobante" = mov."IdMovimientoComprobante"
WHERE TO_DATE(asig."FechaAsignacion") = :FechaInicio
  AND asig."IdBolsa" <> :IdBolsa
  AND "IdEstadoAsignacion" != 'ANU';
