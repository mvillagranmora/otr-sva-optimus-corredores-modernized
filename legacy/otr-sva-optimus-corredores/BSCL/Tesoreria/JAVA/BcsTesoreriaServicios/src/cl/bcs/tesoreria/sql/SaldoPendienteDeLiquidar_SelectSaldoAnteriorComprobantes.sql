SELECT  
	 --B."IdPersonaTitularPago" || '_' || B."IdPortafolioTitularPago" || '_' || B."IdMoneda" AS "Id",
	 B."IdPersonaTitularPago"	as "IdPersona"
	,B."IdPortafolioTitularPago" as "IdPortafolio"
	,B."IdMoneda"
	,SUM(CASE WHEN COALESCE(C."IdComprobanteFormaDePago",0) = 0 
	   THEN (CASE WHEN B."IdTipoMovimiento" = 'MOVING' THEN B."Monto" ELSE 0 END)
	   ELSE (CASE WHEN B."IdTipoMovimiento" = 'MOVING' THEN C."Monto" ELSE 0 END)
	   END) AS "TotalMontoIngresoAnterior"
	,SUM(CASE WHEN COALESCE(C."IdComprobanteFormaDePago",0) = 0 
	   THEN (CASE WHEN B."IdTipoMovimiento" = 'MOVEGR' THEN B."Monto" ELSE 0 END)
	   ELSE (CASE WHEN B."IdTipoMovimiento" = 'MOVEGR' THEN C."Monto" ELSE 0 END)
	   END) AS "TotalMontoEgresoAnterior"
	,SUM(CASE WHEN COALESCE(C."IdComprobanteFormaDePago",0) = 0 
	   THEN (CASE WHEN B."IdTipoMovimiento" = 'MOVEGR' THEN -1*B."Monto" ELSE B."Monto" END)
	   ELSE (CASE WHEN B."IdTipoMovimiento" = 'MOVEGR' THEN -1*C."Monto" ELSE C."Monto" END)
	   END) AS "TotalMontoAnterior"
/*FROM "BSCL.Tesoreria.BD::TES.Comprobante" as B 
LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS C
	ON B."IdComprobante" 			= C."IdComprobante"
	AND C."IdComprobantePartida" 	= 0
	AND C."IdEstado" 				IN ('ING','NDP')
WHERE B."IdPersonaTitularPago" IN (:IdPersona) 
AND (COALESCE(B."IdPortafolioTitularPago",0) = 0 OR B."IdPortafolioTitularPago" IN (:IdPortafolio)) 
AND B."IdMoneda" IN (:IdMoneda)
AND B."FechaLiquidacion" < :FechaDesde
AND B."IdEstado" IN ('ING','GIR','GIP','LIP')*/
FROM "BSCL.Tesoreria.BD::GBL.SaldoPendienteDeLiquidar_Proceso_Paso" SAL
INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as B
	ON (SAL."IdPersona" = B."IdPersonaTitularPago" AND
		(COALESCE(B."IdPortafolioTitularPago",0) = 0 OR B."IdPortafolioTitularPago" = SAL."IdPortafolio") AND
		B."IdMoneda" = SAL."IdMoneda" AND
		B."FechaLiquidacion" < :FechaDesde AND
		B."IdEstado" IN ('ING','GIR','GIP','LIP'))
LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS C
	ON (B."IdComprobante" = C."IdComprobante" AND
		C."IdComprobantePartida" = 0 AND
		C."IdEstado" IN ('ING','NDP'))
GROUP BY B."IdPersonaTitularPago",B."IdPortafolioTitularPago",B."IdMoneda"
;