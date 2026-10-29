INSERT INTO "BSCL.Facturacion.BD::FAC.ComprobanteCobro"(
"IdComprobanteCobro","IdCobro","IdComprobante","IdMonedaCobro","IdMonedaComprobanteCobro","IdMonedaLiquidacionCobro","MontoCobro",
"MontoComprobanteCobro","MontoLiquidacionCobro","Porcentaje","FechaSistema","FechaIngresoRegistro","IdEstado")
VALUES
(
:IdComprobanteCobro,:IdCobro,:IdComprobante,:IdMonedaCobro,:IdMonedaComprobanteCobro,:IdMonedaLiquidacionCobro,:MontoCobro,
:MontoComprobanteCobro,:MontoLiquidacionCobro,:Porcentaje,:FechaSistema,CURRENT_UTCTIMESTAMP,:IdEstado
)