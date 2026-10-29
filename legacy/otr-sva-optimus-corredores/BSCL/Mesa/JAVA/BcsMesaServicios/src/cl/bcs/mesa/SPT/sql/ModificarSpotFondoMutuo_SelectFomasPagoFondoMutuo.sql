SELECT 
FP."IdComprobantePagoOperacion"
,CASE WHEN "IdTipoOperacion" = 'COMSPT' THEN 'MOVING' ELSE 'MOVEGR' END AS "IdTipoMovimiento"
,FP."IdFormaDePago"
,FP."IdMoneda"
,FP."IdMonedaComprobantePago" AS "IdMonedaLiquidacion"
,FP."IdOperacion"
,FP."RecibePaga" AS "RecibePagaPortafolio"
,FP."Monto" AS "MontoFormaDePago"
,FP."AfectarTesoreria"
,FP."FechaLiquidacionComprobantePagoOperacion" AS "FechaLiquidacion"
FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion" FP
WHERE "IdTipoProducto" = 'MEME' AND "IdOperacion" IN (:IdOperacionSpot)