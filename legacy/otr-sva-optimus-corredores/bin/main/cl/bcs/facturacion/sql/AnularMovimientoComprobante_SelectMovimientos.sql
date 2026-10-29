SELECT "IdMovimientoComprobante","IdEstadoFacturacion","IdMovimientoRelacionado","IdOperacion","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdAsignacion",
"IdCliente","NombreLargoCliente","TipoDocumentoIdentidad","DocumentoIdentidad","IdPortafolio","IdTipoOperacion","IdTipoProducto","IdTipoCanalIngreso",
"Cantidad","Precio","Monto","IdMoneda","IdMonedaLiquidacion","ValorTipoCambio","NemotecnicoInstrumento"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOV
WHERE MOV."IdMovimientoComprobante" IN (:idsMovimiento);
