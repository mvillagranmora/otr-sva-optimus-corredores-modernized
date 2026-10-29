SELECT 
	CMPT."IdMovimientoComprobante"
	, CMPT."IdTipoProducto"
	, CMPT."IdBolsa"
 	, COBRO."IdCobro"
	, COALESCE(COBRO."IdMovimientoComprobanteCobro", 0) AS "IdMovimientoComprobanteCobro"
	, COBRO."Porcentaje"
	, COBRO."MontoCobro"
	, COBRO."MontoLiquidacionCobro"
	, COBRO."MontoMovimientoComprobanteCobro"
	, CMPT."Monto"
	, CMPT."MontoLiquidacion"
	, CMPT."MontoMovimientoComprobante"
	, CMPT."IdMoneda" AS "IdMonedaCobro" 
	, CMPT."IdMonedaLiquidacion" AS "IdMonedaLiquidacionCobro"
	, CMPT."IdMonedaMovimientoComprobante" AS "IdMonedaMovimientoComprobanteCobro"
	, CMPT."IdTipoCanalIngreso"
	, CMPT."IdTipoOperacion"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS CMPT
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" AS COBRO ON COBRO."IdMovimientoComprobante" = CMPT."IdMovimientoComprobante" AND COBRO."IdCobro" = :idCobro
WHERE CMPT."IdTipoProducto" = :idTipoProducto AND CMPT."IdPortafolio" = :idPortafolio AND CMPT."IdBolsa" = :idBolsa;