
SELECT 
	"NumeroReferencia","IdIngresoEgresoInformado","TipoComprobante",
	"IdIngresoEgreso","IdTipoMovimiento","IdConcepto","IdCaja","IdMoneda","Monto",
	"FechaLiquidacion","FechaIngresoRegistro","FechaMovimiento","IdTipoPersona","IdPersona",
	"IdTipoDocumentoIdentidad","IdTipoPersonaTitularPago","IdPersonaTitularPago",
	"IdPortafolioTitularPago","SecuenciaAgrupacion","SecuenciaRelacionada",
	"Glosa","IdEmpleadoDigitador","IdSucursalEmpleadoDigitador","IdTipoComprobanteOrigen",		
	"IdComprobanteOrigen","FechaComprobanteOrigen","FechaLiquidacionComprobanteOrigen","IdTipoProducto",
	"IdTipoOperacion","IdBolsa","IdMonedaOperacion","MontoOperacion","FechaParidad","IdTipoReajuste",		
	"ValorParidad","FolioCartera","IdInstrumento","IdTipoInstrumento","IdClasificacionTipoInstrumento",
	"EsBilateral","TitularPago","EsIngresoAutomatico","NeteaComprobante","AgrupaComprobante",
	"MueveCuentaInversion","IdEstado","IdCondicionDeLiquidacion"
FROM "BSCL.Tesoreria.BD::GBL.IngresoEgresoGenerarComprobante_Proceso"
