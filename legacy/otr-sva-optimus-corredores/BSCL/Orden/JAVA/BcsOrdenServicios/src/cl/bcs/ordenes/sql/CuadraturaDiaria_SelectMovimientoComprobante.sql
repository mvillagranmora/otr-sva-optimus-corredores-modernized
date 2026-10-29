SELECT 
	mov."EstadoFacturacion",
	mov."NombreEstadoFacturacion",
	mov."IdMovimientoComprobante",
	mov."IdTipoComprobante",
	mov."NombreTipoComprobante",
	mov."IdConceptoMovimiento",
	mov."NombreConceptoMovimiento",
	mov."DocumentoIdentidad",
	mov."TipoDocumentoIdentidad",
	mov."NombreLargoCliente"
FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" mov
WHERE mov."FechaMovimientoComprobante" = :FechaInicio
  AND mov."IdEstadoFacturacion" != 'ANU'
  AND mov."IdBolsa" = :IdBolsa
  AND mov."IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento;