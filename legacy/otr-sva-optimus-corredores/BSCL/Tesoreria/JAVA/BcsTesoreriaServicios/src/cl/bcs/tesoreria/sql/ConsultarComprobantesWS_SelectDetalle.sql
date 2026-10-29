SELECT 
DET."IdComprobanteDetalle", 
DET."NroLinea", 
DET."IdTipoMovimiento", 
DET."TipoMovimiento", 
DET."NombreTipoMovimiento", 
DET."IdConcepto", 
DET."NombreConcepto", 
DET."IdMoneda",
DET."Monto", 
DET."IdTipoComprobanteOrigen", 
DET."IdComprobanteOrigen", 
DET."FechaComprobanteOrigen", 
DET."IdTipoProducto", 
DET."NombreTipoProducto", 
DET."IdTipoOperacion", 
DET."NombreTipoOperacion", 
DET."IdInstrumento", 
DET."Nemotecnico", 
DET."IdTipoInstrumento",
DET."IdClasificacionTipoInstrumento",  
DET."IdComprobante"

FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteDetalle" DET
WHERE DET."IdComprobante" IN (:IdsComprobantes)