SELECT "IdComprobante","EsVentaCorta","IdTipoComprobante","IdEstadoDTE","IdEstadoSII","IdEstadoComprobante","IdFormaCustodia","IdTipoOperacion",COALESCE("ValorTipoCambio",0) AS "ValorTipoCambio",
       "IdCliente","NombreLargoCliente","TipoDocumentoIdentidad","DocumentoIdentidad","IdPortafolio","IdTipoOperacion","IdTipoProducto","IdTipoCanalIngreso",
       "Cantidad","Precio","MontoLiquidacionFinal","IdMoneda","IdMonedaLiquidacion",IFNULL("NemotecnicoInstrumento",'') AS "NemotecnicoInstrumento",IFNULL("IdTipoInstrumento",'') AS "IdTipoInstrumento","FechaLiquidacion"
FROM "BSCL.Facturacion.BD::FAC.Comprobante"
WHERE "IdComprobante" IN (:idsComprobante) AND
"IdTipoInstrumento" = 'SPT' AND
"IdTipoProducto" = 'MEME'
