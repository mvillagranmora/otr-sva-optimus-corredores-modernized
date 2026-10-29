WITH FFMM as (
	SELECT 
		TO_DATE(:FechaDesde) AS "FechaLiquidacion"
		,CAR."IdPortafolio"
		,CAR."IdMonedaInstrumento" AS "IdMoneda"
		,CAR."INGMontoMercadoActualMonedaInstrumento" AS "Monto"
		,TO_DECIMAL(0,26,8) AS "MontoCompra"
		,TO_DECIMAL(0,26,8) AS "MontoVenta"
		,'CUSTODIA_FFMM_MONEY_MARKET' AS "ItemCalculo"
	FROM "BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso" SAL
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR
		ON (SAL."IdPortafolio" = CAR."IdPortafolio" AND
			SAL."IdMoneda" = CAR."IdMonedaInstrumento" AND
			CAR."IdEstado" = 'VIG' AND
			CAR."IdEmpresa" = :IdEmpresa AND
			CAR."FechaLiquidacion" <= :FechaHasta)
	INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SERIE
		ON (SERIE."IdInstrumento" = CAR."IdInstrumento")
	INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO 
		ON (FONDO."IdFondo" = SERIE."IdFondo"
		AND FONDO."MoneyMarket" = 'S') 
	)
SELECT "IdPortafolio" || '_' || "IdMoneda" AS "Id","FechaLiquidacion","IdPortafolio","IdMoneda",SUM("Monto") as "Monto",SUM("MontoCompra") as "MontoCompra",SUM("MontoVenta") as "MontoVenta","ItemCalculo"
,:TipoCalculoFlujoCaja AS "TipoCalculoFlujoCaja"
FROM FFMM GROUP BY "FechaLiquidacion","IdPortafolio","IdMoneda","ItemCalculo";