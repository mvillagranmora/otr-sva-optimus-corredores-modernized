SELECT 
	 COBRO."IdMovimientoComprobanteCobro"
	, MOVXFACT."IdMovimientoComprobante"
	, TEMP."IdDetallePerfilCobros"
	, MOVXFACT."IdTipoProducto"
	, MOVXFACT."IdTipoCanalIngreso"
	, MOVXFACT."IdTipoOperacion"
	, COBRO."IdMonedaCobro"
	, COBRO."IdMonedaLiquidacionCobro"
	, COBRO."IdMonedaMovimientoComprobanteCobro"
	, TEMP."IdCobro"
	, TEMP."IdBolsa"
	, TEMP."ValorCobro" AS "Porcentaje"
	, COBRO."MontoCobro"
	, COBRO."MontoLiquidacionCobro"
	, COBRO."MontoMovimientoComprobanteCobro"
	, COBRO."Monto"
	, COBRO."MontoLiquidacion"
	, COBRO."MontoMovimientoComprobante"
FROM "#ModificarComisionMovimientosPorDetallePerfilCobro_DetallePerfilCobro" AS TEMP
INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" MOVXFACT 
	ON MOVXFACT."IdPortafolio" = TEMP."IdPortafolio"
	AND MOVXFACT."EstadoFacturacion" = 'ING'
	AND MOVXFACT."IdBolsa" = TEMP."IdBolsa"
	AND MOVXFACT."IdTipoProducto" = TEMP."IdTipoProducto"
INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" AS COBRO
	ON COBRO."IdMovimientoComprobante" = MOVXFACT."IdMovimientoComprobante" 
	AND COBRO."IdCobro" = TEMP."IdCobro"
	AND COBRO."IdEstado" = 'VIG'
	;