SELECT "IdComprobante","EsVentaCorta","IdTipoComprobante","IdEstadoDTE","IdEstadoSII","IdEstadoComprobante","IdFormaCustodia","IdTipoOperacion",COALESCE("ValorTipoCambio",0) AS "ValorTipoCambio",
"IdCliente","NombreLargoCliente","TipoDocumentoIdentidad","DocumentoIdentidad","IdPortafolio","IdTipoOperacion",
"Cantidad","Precio","MontoLiquidacionFinal","IdMoneda","IdMonedaLiquidacion","NemotecnicoInstrumento"
FROM "BSCL.Facturacion.BD::FAC.Comprobante"
WHERE "IdComprobante" IN (:idsComprobante)


