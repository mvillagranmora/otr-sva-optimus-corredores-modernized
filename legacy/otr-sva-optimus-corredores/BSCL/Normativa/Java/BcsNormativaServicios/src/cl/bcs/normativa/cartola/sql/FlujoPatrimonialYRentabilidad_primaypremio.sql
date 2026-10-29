SELECT 
"IdTipoOperacion"
,"IdTipoProducto"
,sum("MontoLiquidacionFinal") AS "MontoFinalLiquidacion"
FROM "BSCL.Facturacion.BD::FAC.Comprobante"
WHERE "IdTipoOperacion" IN ('ANTPRT','ANTEMP','VENPRT','VENEMP')
AND "IdCliente"      =:p_IdCliente
AND "IdPortafolio"   =:p_IdPortafolio
AND "FechaLiquidacion" BETWEEN :p_FechaOp1 AND :p_FechaOp2
GROUP BY "IdTipoOperacion","IdTipoProducto"