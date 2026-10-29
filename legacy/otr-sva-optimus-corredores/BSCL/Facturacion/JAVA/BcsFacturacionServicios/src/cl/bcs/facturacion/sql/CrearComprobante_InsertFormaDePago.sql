INSERT INTO "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago"( 
"IdComprobanteFormaDePago","IdComprobante","IdMedioDePago","NombreMedioDePago","NumeroSerie","NumeroDocumento","IdCuentaBancaria","NombreCuentaBancaria","IdTipoCuentaBancaria",
"NombreTipoCuentaBancaria","NumeroCuentaBancaria","IdTarjetaBancaria","IdTipoTarjetaBancaria","NombreTipoTarjetaBancaria","NumeroTarjetaBancaria","FechaVctoTarjetaBancaria",
"IdModalidadDePago","NombreModalidadDePago","IdBanco","CodigoBanco","NombreBanco","IdMoneda","FechaDocumentoPago","MontoLiquidacionFormaDePago","IdEstado","FechaIngresoRegistro"
 )
 VALUES
 (
 :IdComprobanteFormaDePago,:IdComprobante,:IdMedioDePago,:NombreMedioDePago,:NumeroSerie,:NumeroDocumento,:IdCuentaBancaria,:NombreCuentaBancaria,:IdTipoCuentaBancaria,
:NombreTipoCuentaBancaria,:NumeroCuentaBancaria,:IdTarjetaBancaria,:IdTipoTarjetaBancaria,:NombreTipoTarjetaBancaria,:NumeroTarjetaBancaria,:FechaVctoTarjetaBancaria,
:IdModalidadDePago,:NombreModalidadDePago,:IdBanco,:CodigoBanco,:NombreBanco,:IdMoneda,:FechaDocumentoPago,:MontoLiquidacionFormaDePago,:IdEstado,CURRENT_UTCTIMESTAMP
 )