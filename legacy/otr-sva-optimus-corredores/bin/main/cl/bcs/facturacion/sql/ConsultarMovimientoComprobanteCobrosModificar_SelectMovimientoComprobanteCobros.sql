SELECT 
COBRO."IdCobro" AS "IdTipoCobro"
,COBRO."NombreCobro" AS "NombreTipoCobro"
,COBRO_CLASIFICACION."DescripcionCobroClasificacion" AS "NombreClasificacionCobro"
,CASE COBRO."FormaCobro"
	WHEN 'F' THEN 'FIJA'
	WHEN 'V' THEN 'VARIABLE'
END AS "NombreFormaCobro"
,MOVIMIENTO_COMPROBANTE_COBRO."IdMonedaCobro" AS "IdMonedaCobro"
,SUM(MOVIMIENTO_COMPROBANTE_COBRO."MontoCobro") AS "MontoCobro"
,SUM(MOVIMIENTO_COMPROBANTE_COBRO."MontoCobro") AS "MontoCobroOriginal"
,MOVIMIENTO_COMPROBANTE_COBRO."IdMonedaMovimientoComprobanteCobro" AS "IdMonedaCobroSistema"
,SUM(MOVIMIENTO_COMPROBANTE_COBRO."MontoMovimientoComprobanteCobro") AS "MontoLocal"
,MOVIMIENTO_COMPROBANTE_COBRO."IdMonedaLiquidacionCobro" AS "IdMonedaCobroLiquidacion"
,SUM(MOVIMIENTO_COMPROBANTE_COBRO."MontoLiquidacionCobro") AS "MontoLiquidacion"
,AVG(MOVIMIENTO_COMPROBANTE_COBRO."Porcentaje") AS "Porcentaje"
,ESTADO."Nombre" AS "Estado"
,COBRO."EsRecalculable" AS "Recalculable"
,SUM(MOVIMIENTO_COMPROBANTE_COBRO."MontoMovimientoComprobante") AS "MontoOperacion"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" AS MOVIMIENTO_COMPROBANTE_COBRO
INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" AS COBRO ON COBRO."IdCobro" = MOVIMIENTO_COMPROBANTE_COBRO."IdCobro"
INNER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" AS COBRO_CLASIFICACION ON COBRO_CLASIFICACION."IdCobroClasificacion" = COBRO."IdCobroClasificacion"
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTADO ON ESTADO."IdEstado" = MOVIMIENTO_COMPROBANTE_COBRO."IdEstado"
WHERE MOVIMIENTO_COMPROBANTE_COBRO."IdCobro" = :IdCobro 
AND MOVIMIENTO_COMPROBANTE_COBRO."IdMovimientoComprobante" IN (:IdMovimientoComprobante)
AND MOVIMIENTO_COMPROBANTE_COBRO."IdEstado" = 'VIG'
GROUP BY
COBRO."IdCobro"
,COBRO."NombreCobro"
,COBRO_CLASIFICACION."DescripcionCobroClasificacion"
,COBRO."FormaCobro"
,MOVIMIENTO_COMPROBANTE_COBRO."IdMonedaCobro"
,MOVIMIENTO_COMPROBANTE_COBRO."IdMonedaMovimientoComprobanteCobro"
,MOVIMIENTO_COMPROBANTE_COBRO."IdMonedaLiquidacionCobro"
,ESTADO."Nombre"
,COBRO."EsRecalculable";