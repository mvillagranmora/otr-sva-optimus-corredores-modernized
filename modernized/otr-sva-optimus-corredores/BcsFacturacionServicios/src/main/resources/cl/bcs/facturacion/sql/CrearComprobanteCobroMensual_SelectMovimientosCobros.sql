SELECT "IdMovimientoComprobanteCobro" AS "IdMovimientoComprobanteCobroOld", 
       "IdCobro", 
       "IdMovimientoComprobante" AS "NumeroMvtoCmbte",
       "IdMovimientoComprobanteCobro" AS "NumeroMvtoCmbteCobro",
       MvtoCmbteCobro."IdMonedaCobro",
       MvtoCmbteCobro."MontoCobro", 
       "Porcentaje", 
       MvtoCmbteCobro."FechaSistema", 
       "IdEstado", 
       MvtoCmbteCobro."IdMonedaLiquidacionCobro", 
       "IdMonedaMovimientoComprobanteCobro", 
       "MontoMovimientoComprobanteCobro",
       MvtoCmbteCobro."MontoLiquidacionCobro", 
       "Monto", 
       "MontoMovimientoComprobante", 
       "MontoLiquidacion"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" MvtoCmbteCobro
WHERE MvtoCmbteCobro."IdMovimientoComprobante" IN (:idsMovimientoComprobanteOld)
  AND MvtoCmbteCobro."IdEstado" = 'VIG';