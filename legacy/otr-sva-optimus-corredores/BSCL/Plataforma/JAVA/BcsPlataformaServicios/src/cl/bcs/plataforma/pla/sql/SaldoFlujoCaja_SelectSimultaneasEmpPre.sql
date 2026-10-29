WITH SMEP as (
	SELECT (CASE WHEN CAR."FechaLiquidacion" >= :FechaHasta THEN TO_DATE(CAR."FechaLiquidacion") ELSE TO_DATE(:FechaDesde) END) AS "FechaLiquidacion"
		,CAR."IdPortafolio"
		,CAR."IdMonedaSistema" AS "IdMoneda"
		,CASE WHEN CAR."IdTipoInstrumento" = 'EMP' OR CAR."IdTipoInstrumento" = 'PRT' THEN CAR."INGMontoATasaCompraActual" ELSE TO_DECIMAL(0,26,8) END AS "Monto"
		,CASE WHEN CAR."IdTipoOperacion" = 'COMSIM' THEN CAR."INGMontoATasaCompraActual" ELSE TO_DECIMAL(0,26,8) END "MontoCompra"				
		,CASE WHEN CAR."IdTipoOperacion" = 'VENSIM' THEN CAR."INGMontoATasaCompraActual" ELSE TO_DECIMAL(0,26,8) END "MontoVenta"					
		,CASE WHEN CAR."IdTipoOperacion" = 'COMSIM' THEN 'SIMULTANEA_COMPRAS'
			  WHEN CAR."IdTipoOperacion" = 'VENSIM' THEN 'SIMULTANEA_VENTAS'
			  WHEN CAR."IdTipoInstrumento" = 'EMP' THEN 'EMPRESTITOS'
			  WHEN CAR."IdTipoInstrumento" = 'PRT' THEN 'PRESTAMOS'
		END AS "ItemCalculo"
	/*FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraFlujoCaja" AS CAR 
	WHERE CAR."IdPortafolio" IN (:IdPortafolio)
	AND CAR."IdMonedaSistema" IN (:IdMoneda)
	AND CAR."IdTipoInstrumento" IN ('SIM','EMP','PRT')
	AND CAR."IdEmpresa" = :IdEmpresa
	AND CAR."IdEstado"  = 'VIG'   -- Solo Vigentes
	AND CAR."FechaLiquidacion" <= :FechaHasta*/
	FROM "BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso" SAL
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraFlujoCaja" AS CAR
		ON (SAL."IdPortafolio" = CAR."IdPortafolio" AND
			SAL."IdMoneda" = CAR."IdMonedaSistema" AND
			CAR."IdTipoInstrumento" IN ('SIM','EMP','PRT') AND
			CAR."IdEmpresa" = :IdEmpresa AND
			CAR."IdEstado" = 'VIG' AND
			CAR."FechaLiquidacion" <= :FechaHasta)
	)
SELECT "IdPortafolio" || '_' || "IdMoneda" AS "Id","FechaLiquidacion","IdPortafolio","IdMoneda",SUM("Monto") as "Monto",SUM("MontoCompra") as "MontoCompra",SUM("MontoVenta") as "MontoVenta","ItemCalculo"
,:TipoCalculoFlujoCaja AS "TipoCalculoFlujoCaja"
FROM SMEP GROUP BY "FechaLiquidacion","IdPortafolio","IdMoneda","ItemCalculo";