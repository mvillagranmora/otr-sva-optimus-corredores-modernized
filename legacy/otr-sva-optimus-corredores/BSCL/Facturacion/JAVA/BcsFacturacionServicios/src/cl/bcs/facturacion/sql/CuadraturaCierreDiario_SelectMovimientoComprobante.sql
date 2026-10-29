SELECT 
	ESTADOFACTURACION."IdEstado" As "EstadoFacturacion",
	ESTADOPLA."Nombre" As "NombreEstadoFacturacion",
	MovComprobante."IdMovimientoComprobante",
	MovComprobante."IdTipoComprobante",
	TIPOCOMPROBANTE."Nombre" As "NombreTipoComprobante",
	MovComprobante."IdConceptoMovimiento",
	CONCEPTOMOV."Nombre" As "NombreConceptoMovimiento",
	MovComprobante."DocumentoIdentidad",
	MovComprobante."TipoDocumentoIdentidad",
	MovComprobante."NombreLargoCliente",
	MovComprobante."IdEstadoFacturacion"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MovComprobante
INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CONCEPTOMOV   ON CONCEPTOMOV."IdConcepto" = MovComprobante."IdConceptoMovimiento"
LEFT JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS TIPOCOMPROBANTE      ON TIPOCOMPROBANTE."IdTipoComprobante" = MovComprobante."IdTipoComprobante"
LEFT JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS ESTADOFACTURACION     ON ESTADOFACTURACION."IdEstado" = MovComprobante."IdEstadoFacturacion" AND Upper(EstadoFacturacion."NombreEntidad") = Upper('FAC.MovimientoComprobante')  
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTADOPLA      ON ESTADOPLA."IdEstado" = ESTADOFACTURACION."IdEstado"   
WHERE MovComprobante."FechaMovimientoComprobante" = :fechaInicio AND MovComprobante."IdTipoOperacion" = :IdTipoOperacion
AND MovComprobante."IdEstadoFacturacion" != 'ANU';
