WITH EVC as (
	SELECT (CASE WHEN EVC."FechaPago" >= :FechaHasta THEN TO_DATE(EVC."FechaPago") ELSE TO_DATE(:FechaDesde) END) AS "FechaLiquidacion"
		,EVC."IdPortafolio"
		,EVC."IdMoneda"
		,EVC."Total" as "Monto"
		,TO_DECIMAL(0,26,8) as "MontoCompra"
		,TO_DECIMAL(0,26,8) as "MontoVenta"
		,CASE WHEN EVC."IdTipoEventoCapital" = 'DV' THEN 'DIVIDENDOS_EN_DINERO'
		WHEN EVC."IdTipoEventoCapital" = 'CC' THEN 'CORTE_CUPON'
		WHEN EVC."IdTipoEventoCapital" = 'SL' THEN 'SORTEO_DE_LETRAS'
		END	AS "ItemCalculo"
	/*FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteFlujoCaja" AS EVC
	WHERE EVC."IdPortafolio" IN (:IdPortafolio)
	AND EVC."IdMoneda" IN (:IdMoneda)
	AND EVC."IdTipoEventoCapital" IN ('DV','CC','SL')
	AND EVC."IdEstado" IN ('ING','VIG','NOT')  -- Considera todos los estados intermedios(Cerrado es estado final)
	AND EVC."IdEmpresa" = :IdEmpresa
	AND EVC."FechaPago" <= :FechaHasta*/
	FROM "BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso" SAL
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteFlujoCaja" AS EVC
		ON (SAL."IdPortafolio" = EVC."IdPortafolio" AND
			SAL."IdMoneda" = EVC."IdMoneda" AND
			EVC."IdTipoEventoCapital" IN ('DV','CC','SL') AND
			EVC."IdEstado" IN ('ING','VIG','NOT') AND
			EVC."IdEmpresa" = :IdEmpresa AND
			EVC."FechaPago" <= :FechaHasta)
	)
SELECT "IdPortafolio" || '_' || "IdMoneda" AS "Id","FechaLiquidacion","IdPortafolio","IdMoneda",SUM("Monto") as "Monto",SUM("MontoCompra") as "MontoCompra",SUM("MontoVenta") as "MontoVenta","ItemCalculo"
,:TipoCalculoFlujoCaja AS "TipoCalculoFlujoCaja"
FROM EVC GROUP BY "FechaLiquidacion","IdPortafolio","IdMoneda","ItemCalculo";