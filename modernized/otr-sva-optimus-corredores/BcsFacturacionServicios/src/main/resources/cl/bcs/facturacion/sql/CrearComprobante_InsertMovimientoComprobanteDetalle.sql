INSERT INTO "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" (
"IdMovimientoComprobanteDetalle","IdComprobanteDetalle","IdMovimientoComprobante","ModPrefFact","IdTransaccion","IdInstrumento","NemotecnicoInstrumento","IdTipoInstrumento","NombreTipoInstrumento",
"IdClasificacionTipoInstrumento","NombreClasificacionTipoInstrumento","IdInstrumentoSubyacente","NemotecnicoSubyacente","IdTipoInstrumentoSubyacente","NombreTipoInstrumentoSubyacente","IdInstrumentoRelacionado",
"NemotecnicoRelacionado","IdTipoInstrumentoRelacionado","NombreTipoInstrumentoRelacionado","IdClasificacionTipoInstrumentoRelacionado","NombreClasificacionTipoInstrumentoRelacionado","IdConceptoMovimiento",
"Cantidad","Precio","PrecioRaiz","PrecioPromedio","Monto","MontoRaiz","MontoMovimientoComprobanteDetalle","MontoLiquidacion","Tasa","TasaEmision","Spread","ValorRescate","IdMoneda","IdMesa","IdMonedaMovimientoComprobanteDetalle",
"IdMonedaLiquidacion","IdMonedaSubyacente","ValorParidad","ValorCaptacionMonedaOrigen","SecuenciaAgrupacion","IdEstadoMovimientoComprobanteDetalle","FechaIngresoRegistro","IdEmpleadoEjecutivoPortafolio",
"IdSucursalEmpleadoEjecutivoPortafolio","CodigoIFRS","IdTipoReajuste","FolioTransaccion","FechaTransaccion","EsEnRueda","IndicadorOrdenDirecta","EsVentaCorta","ValorTipoCambio","IdFormaCustodia","NemoSVS","Glosa","IdMonedaEmision", "IdMonedaSpot")
VALUES
(
:IdMovimientoComprobanteDetalle,:IdComprobanteDetalle,:IdMovimientoComprobante,:ModPrefFact,:IdTransaccion,:IdInstrumento,:NemotecnicoInstrumento,:IdTipoInstrumento,:NombreTipoInstrumento,
:IdClasificacionTipoInstrumento,:NombreClasificacionTipoInstrumento,:IdInstrumentoSubyacente,:NemotecnicoSubyacente,:IdTipoInstrumentoSubyacente,:NombreTipoInstrumentoSubyacente,:IdInstrumentoRelacionado,
:NemotecnicoRelacionado,:IdTipoInstrumentoRelacionado,:NombreTipoInstrumentoRelacionado,:IdClasificacionTipoInstrumentoRelacionado,:NombreClasificacionTipoInstrumentoRelacionado,:IdConceptoMovimiento,
:Cantidad,:Precio,:PrecioRaiz,:PrecioPromedio,:Monto,:MontoRaiz,:MontoMovimientoComprobanteDetalle,:MontoLiquidacion,:Tasa,:TasaEmision,:Spread,:ValorRescate,:IdMoneda,:IdMesa,:IdMonedaMovimientoComprobanteDetalle,
:IdMonedaLiquidacion,:IdMonedaSubyacente,:ValorParidad,:ValorCaptacionMonedaOrigen,:SecuenciaAgrupacion,:IdEstadoMovimientoComprobanteDetalle,CURRENT_UTCTIMESTAMP,:IdEmpleadoEjecutivoPortafolio,
:IdSucursalEmpleadoEjecutivoPortafolio,:CodigoIFRS,:IdTipoReajuste,:FolioTransaccion,:FechaTransaccion,:EsEnRueda,:IndicadorOrdenDirecta,:EsVentaCorta,:ValorTipoCambio,:IdFormaCustodia,:NemoSVS,:Glosa,:IdMonedaEmision,:IdMonedaSpot
)
