INSERT INTO "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago" ( 
"IdMovimientoComprobanteFormaDePago","IdMovimientoComprobante","IdTipoMovimiento","IdMedioDePago","NombreMedioDePago","IdCuentaBancaria","NombreCuentaBancaria","IdTipoCuentaBancaria",
"NombreTipoCuentaBancaria","NumeroCuentaBancaria","IdTarjetaBancaria","IdTipoTarjetaBancaria","NombreTipoTarjetaBancaria","NumeroTarjetaBancaria","IdModalidadDePago","NombreModalidadDePago",
"IdBanco","CodigoBanco","NombreBanco","MontoLiquidacionFormaDePago","IdEstado","FechaIngresoRegistro","IdMoneda","FechaVctoTarjetaBancaria","NumeroSerie","NumeroDocumento","FechaDocumentoPago",
"IdTipoMovimientoSpot","AfectarTesoreria","AfectarCuentaInversion")
VALUES(
:IdMovimientoComprobanteFormaDePago,:IdMovimientoComprobante,:IdTipoMovimiento,:IdMedioDePago,:NombreMedioDePago,:IdCuentaBancaria,:NombreCuentaBancaria,:IdTipoCuentaBancaria,
:NombreTipoCuentaBancaria,:NumeroCuentaBancaria,:IdTarjetaBancaria,:IdTipoTarjetaBancaria,:NombreTipoTarjetaBancaria,:NumeroTarjetaBancaria,:IdModalidadDePago,:NombreModalidadDePago,
:IdBanco,:CodigoBanco,:NombreBanco,:MontoLiquidacionFormaDePago,:IdEstado,CURRENT_UTCTIMESTAMP,:IdMoneda,:FechaVctoTarjetaBancaria,:NumeroSerie,:NumeroDocumento,:FechaDocumentoPago,
:IdTipoMovimientoSpot,:AfectarTesoreria,:AfectarCuentaInversion
)