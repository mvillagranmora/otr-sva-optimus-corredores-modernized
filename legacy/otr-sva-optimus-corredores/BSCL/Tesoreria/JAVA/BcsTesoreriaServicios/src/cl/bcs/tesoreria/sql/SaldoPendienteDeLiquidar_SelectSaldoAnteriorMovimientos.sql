SELECT  
	--B."IdPersonaTitularPago" || '_' || B."IdPortafolioTitularPago" || '_' || B."IdMoneda" AS "Id",
	 B."IdPersonaTitularPago"	as "IdPersona"
	,B."IdPortafolioTitularPago" as "IdPortafolio"
	,B."IdMoneda"
	,SUM(CASE WHEN COALESCE(C."IdIngresoEgresoFormaDePago",0) = 0 
		THEN (CASE WHEN B."IdTipoMovimiento" = 'MOVING' THEN B."Monto" ELSE 0 END)
		ELSE (CASE WHEN B."IdTipoMovimiento" = 'MOVING' THEN C."Monto" ELSE 0 END)
	   END) AS "TotalMontoIngresoAnterior"
	,SUM(CASE WHEN COALESCE(C."IdIngresoEgresoFormaDePago",0) = 0 
		THEN (CASE WHEN B."IdTipoMovimiento" = 'MOVEGR' THEN B."Monto" ELSE 0 END)
		ELSE (CASE WHEN B."IdTipoMovimiento" = 'MOVEGR' THEN C."Monto" ELSE 0 END)
	   END) AS "TotalMontoEgresoAnterior"
	,SUM(CASE WHEN COALESCE(C."IdIngresoEgresoFormaDePago",0) = 0 
		THEN (CASE WHEN B."IdTipoMovimiento" = 'MOVEGR' THEN -1*B."Monto" ELSE B."Monto" END)
		ELSE (CASE WHEN B."IdTipoMovimiento" = 'MOVEGR' THEN -1*C."Monto" ELSE C."Monto" END)
	   END) AS "TotalMontoAnterior"
/*FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" as B
LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" AS C
	ON (B."IdIngresoEgreso" = C."IdIngresoEgreso" AND
		C."IdEstado" = 'ING')
WHERE B."IdPersonaTitularPago" IN (:IdPersona) 
AND (COALESCE(B."IdPortafolioTitularPago",0) = 0 OR B."IdPortafolioTitularPago" IN (:IdPortafolio)) 
AND B."IdMoneda" IN (:IdMoneda)
AND B."FechaLiquidacion" < :FechaDesde
AND B."IdEstado" = 'ING'*/
FROM "BSCL.Tesoreria.BD::GBL.SaldoPendienteDeLiquidar_Proceso_Paso" SAL
INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" as B
	ON (SAL."IdPersona" = B."IdPersonaTitularPago" AND
		(COALESCE(B."IdPortafolioTitularPago",0) = 0 OR B."IdPortafolioTitularPago" = SAL."IdPortafolio") AND
		B."IdMoneda" = SAL."IdMoneda" AND
		B."FechaLiquidacion" < :FechaDesde AND
		B."IdEstado" = 'ING')
LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" AS C
	ON (B."IdIngresoEgreso" = C."IdIngresoEgreso" AND
		C."IdEstado" = 'ING')
GROUP BY B."IdPersonaTitularPago",B."IdPortafolioTitularPago",B."IdMoneda"
;