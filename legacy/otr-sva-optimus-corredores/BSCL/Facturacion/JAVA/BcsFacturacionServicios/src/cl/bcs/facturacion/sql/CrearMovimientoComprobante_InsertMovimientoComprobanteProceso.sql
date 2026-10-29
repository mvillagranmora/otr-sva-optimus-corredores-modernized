INSERT INTO "BSCL.Facturacion.BD::FAC.CrearMovimientoComprobante_Proceso"
("NumeroMvtoCmbte", "IdConceptoMovimiento", "IdTipoComprobante", "IdCondicionDeLiquidacion","IdTipoReajuste","IdTipoConvenio","IdEmisor","IdMonedaLiquidacion","IdInstrumento", 
"IdInstrumentoRelacionado", "IdEmpleadoDigitador", "IdInstrumentoSubyacente","IdTipoProducto","IdMoneda","IdMonedaSubyacente","IdTipoCanalIngreso","IdTipoOperacion", 
"IdBoveda","IdCorredoraContraparte","IdBolsa","IdOrden","IdPortafolio","IdComprobanteReferencia","IdMonedaSpot")
VALUES(:NumeroMvtoCmbte,:IdConceptoMovimiento,:IdTipoComprobante,:IdCondicionLiquidacion,:IdTipoReajuste,:IdTipoConvenio,:IdEmisor,:IdMonedaLiquidacion,:IdInstrumento, 
:IdInstrumentoRelacionado,:IdEmpleadoDigitador,:IdInstrumentoSubyacente,:IdTipoProducto,:IdMoneda,:IdMonedaSubyacente,:IdTipoCanalIngreso,:IdTipoOperacion, 
:IdBoveda,:IdCorredoraContraparte,:IdBolsa,:IdOrden,:IdPortafolio,:IdComprobanteReferencia,:IdMonedaSpot);
