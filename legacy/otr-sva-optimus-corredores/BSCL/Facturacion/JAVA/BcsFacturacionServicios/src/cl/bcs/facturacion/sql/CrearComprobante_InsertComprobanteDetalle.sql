INSERT INTO "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" (
"IdComprobanteDetalle","IdComprobante","IdConcepto","NroLinea","ModPrefFact","FechaLiquidacion","IdInstrumento","NemotecnicoInstrumento","IdTipoInstrumento","NombreTipoInstrumento",
"IdClasificacionTipoInstrumento","NombreClasificacionTipoInstrumento","IdInstrumentoSubyacente","NemotecnicoSubyacente","IdTipoInstrumentoSubyacente","NombreTipoInstrumentoSubyacente",
"IdInstrumentoRelacionado","NemotecnicoRelacionado","IdTipoInstrumentoRelacionado","NombreTipoInstrumentoRelacionado","IdClasificacionTipoInstrumentoRelacionado","NombreClasificacionTipoInstrumentoRelacionado",
"IdTipoOperacion","IdTipoProducto","Cantidad","Precio","Monto","IdMesa","IdBolsa","IdEstadoComprobanteDetalle","FechaIngresoRegistro","IdCarga","FolioComprobanteFacturacionMigracion",
"MontoLiquidacion","IdMoneda","IdMonedaLiquidacion","IdMonedaComprobanteDetalle","IdMonedaSubyacente","IdMonedaSpot","MontoComprobanteDetalle","FechaComprobanteDetalle","IdTransaccion","FolioTransaccion","FechaTransaccion",
"IdEmpleadoEjecutivoPortafolio","IdSucursalEmpleadoEjecutivoPortafolio","EsVentaCorta","ValorTipoCambio","IndicadorOrdenDirecta","IdFormaCustodia","NemoSVS", "CodigoIFRS","ValorRescate","Tasa", "ValorParidad","GlosaSimplificada")
VALUES
(
:IdComprobanteDetalle,:IdComprobante,:IdConcepto,:NroLinea,:ModPrefFact,:FechaLiquidacion,:IdInstrumento,:NemotecnicoInstrumento,:IdTipoInstrumento,:NombreTipoInstrumento,
:IdClasificacionTipoInstrumento,:NombreClasificacionTipoInstrumento,:IdInstrumentoSubyacente,:NemotecnicoSubyacente,:IdTipoInstrumentoSubyacente,:NombreTipoInstrumentoSubyacente,
:IdInstrumentoRelacionado,:NemotecnicoRelacionado,:IdTipoInstrumentoRelacionado,:NombreTipoInstrumentoRelacionado,:IdClasificacionTipoInstrumentoRelacionado,:NombreClasificacionTipoInstrumentoRelacionado,
:IdTipoOperacion,:IdTipoProducto,:Cantidad,:Precio,:Monto,:IdMesa,:IdBolsa,:IdEstadoComprobanteDetalle,CURRENT_UTCTIMESTAMP,:IdCarga,:FolioComprobanteFacturacionMigracion,
:MontoLiquidacion,:IdMoneda,:IdMonedaLiquidacion,:IdMonedaComprobanteDetalle,:IdMonedaSubyacente,:IdMonedaSpot,:MontoComprobanteDetalle,:FechaComprobanteDetalle,:IdTransaccion,:FolioTransaccion,:FechaTransaccion,
:IdEmpleadoEjecutivoPortafolio,:IdSucursalEmpleadoEjecutivoPortafolio,:EsVentaCorta,:ValorTipoCambio,:IndicadorOrdenDirecta,:IdFormaCustodia,:NemoSVS,:CodigoIFRS,:ValorRescate,:Tasa, :ValorParidad, :GlosaSimplificada
)
