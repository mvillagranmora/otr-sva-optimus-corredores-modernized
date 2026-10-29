SELECT COB."IdMovimientoComprobante",COB."IdCobro",COB."IdMovimientoComprobanteCobro",COB."Porcentaje",
       COB."IdMonedaMovimientoComprobanteCobro",COB."MontoMovimientoComprobanteCobro",
       COB."IdMonedaCobro",COB."MontoCobro",
       COB."IdMonedaLiquidacionCobro",COB."MontoLiquidacionCobro",
       COB."Monto",COB."MontoLiquidacion",COB."MontoMovimientoComprobante"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" COB
  INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MVTO
     ON MVTO."IdMovimientoComprobante" = COB."IdMovimientoComprobante"
    AND MVTO."ModPrefFact"= 'MODPREFNORM'
    AND COB."IdEstado" = 'VIG'
WHERE COB."IdMovimientoComprobante" IN (:idsMovimientoComprobante);