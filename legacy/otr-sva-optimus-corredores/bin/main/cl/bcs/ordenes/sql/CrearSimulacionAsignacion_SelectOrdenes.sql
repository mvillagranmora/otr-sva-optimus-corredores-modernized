SELECT ord."IdOrden","IdBoveda","IdCuentaCustodioPortafolio","IdMonedaLiquidacionOrden","IdFormaCustodia","IdCorredorCorresponsal" AS "IdCorredoraCorresponsal",
"TasaVentaCorta","PlazoVentaCorta","TipoCambio",pag."IdPreferenciaPagoPortafolio",pag."IdMedioDePago"
FROM "BSCL.Orden.BD::ORD.Orden" ord
LEFT JOIN "BSCL.Orden.BD::ORD.FormaDePagoOrden" pag ON pag."IdOrden" = ord."IdOrden"
WHERE ord."IdOrden" IN (:idsOrdenes)
;