SELECT CAST("MontoLiquidacion" AS DECIMAL(26,8)) AS "total"
FROM "BSCL.Facturacion.BD::FAC.Comprobante"
WHERE "IdTipoComprobante" = 'FEE'
    AND "IdComprobanteReferencia" = 0
    AND "IdMoneda" IN ('CLP', 'USD')
    AND "FechaLiquidacion" = TO_VARCHAR(:p_FechaPosicion, 'YYYY-MM-DD') 
    AND "IdTipoOperacion" = :IdTipoOperacion    
    AND "IdCondicionLiquidacion" = 'T0'
    AND "IdEstadoComprobante" NOT IN ('ANU', 'ERR', 'PEN');