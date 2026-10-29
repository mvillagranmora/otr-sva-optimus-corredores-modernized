SELECT  cob."IdMovimientoComprobanteCobro" AS "IdMovimientoComprobanteCobroOld",
        cob."IdCobro", 
        cob."IdMovimientoComprobante" AS "NumeroMvtoCmbte",
        cob."IdMovimientoComprobanteCobro" AS "NumeroMvtoCmbteCobro",
        cob."IdMonedaCobro",
        cob."MontoCobro", 
        cob."Porcentaje", 
        cob."FechaSistema", 
        cob."IdEstado", 
        cob."IdMonedaLiquidacionCobro",
        cob."IdMonedaMovimientoComprobanteCobro",
        cob."MontoMovimientoComprobanteCobro",
        cob."MontoLiquidacionCobro",
        cob."Monto",
        cob."MontoMovimientoComprobante",
        cob."MontoLiquidacion"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" cob
WHERE cob."IdMovimientoComprobante" IN (:idsMovimientoComprobanteOld)
  AND cob."IdEstado" = 'VIG';