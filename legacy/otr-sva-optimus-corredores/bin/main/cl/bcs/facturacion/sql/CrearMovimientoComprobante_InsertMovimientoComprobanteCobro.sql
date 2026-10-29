INSERT INTO "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" (
"IdMovimientoComprobanteCobro","IdCobro","IdMovimientoComprobante","Porcentaje","FechaSistema","FechaIngresoRegistro","IdEstado",
"IdMonedaMovimientoComprobanteCobro","MontoMovimientoComprobanteCobro","MontoCobro","MontoLiquidacionCobro","IdMonedaCobro",
"IdMonedaLiquidacionCobro","Monto","MontoLiquidacion","MontoMovimientoComprobante")
VALUES(
:IdMovimientoComprobanteCobro,:IdCobro,:IdMovimientoComprobante,:Porcentaje,:FechaSistema,CURRENT_UTCTIMESTAMP,:IdEstado,
:IdMonedaMovimientoComprobanteCobro,:MontoMovimientoComprobanteCobro,:MontoCobro,:MontoLiquidacionCobro,:IdMonedaCobro,
:IdMonedaLiquidacionCobro,:Monto,:MontoLiquidacion,:MontoMovimientoComprobante
)