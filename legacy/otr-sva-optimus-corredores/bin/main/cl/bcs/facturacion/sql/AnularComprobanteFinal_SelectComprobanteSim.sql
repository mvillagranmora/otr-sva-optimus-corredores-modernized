SELECT MovRel."IdComprobante",Cmbte."EsVentaCorta",Cmbte."IdTipoComprobante","IdEstadoDTE","IdEstadoSII","IdEstadoComprobante",Cmbte."IdFormaCustodia",Cmbte."IdTipoOperacion",Cmbte."IdTipoInstrumento",
       COALESCE(Cmbte."ValorTipoCambio",0) AS "ValorTipoCambio",
       Cmbte."IdCliente",Cmbte."NombreLargoCliente",Cmbte."TipoDocumentoIdentidad",Cmbte."DocumentoIdentidad",Cmbte."IdPortafolio",Cmbte."IdTipoOperacion",
       Cmbte."Cantidad",Cmbte."Precio","MontoLiquidacionFinal",Cmbte."IdMoneda",Cmbte."IdMonedaLiquidacion",Cmbte."NemotecnicoInstrumento", Cmbte."IdTipoInstrumento",Cmbte."FechaLiquidacion"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" Mov
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MovRel
    ON MovRel."IdMovimientoRelacionado" = Mov."IdMovimientoComprobante"
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" Cmbte
     ON Cmbte."IdComprobante" = Mov."IdComprobante"
WHERE Cmbte."IdComprobante" IN (:idsComprobante)
