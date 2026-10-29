WITH evento_capital_30 AS (
	SELECT
		"Factor",
		"IdInstrumento",
		"IdMoneda" AS "Moneda",
		"IdMonedaLiquidacion" ,
		"FechaPago" ,
		"IdEventoCapital",
		"Nemo",
		"Paridad"
	FROM "BSCL.Custodia.BD::CUS.EventoCapital"
	WHERE
		"FechaPago" BETWEEN :FechaDesde AND ADD_DAYS(:FechaDesde, 30)
		AND "IdTipoEventoCapital" IN ('DV', 'RC')
		AND "IdEstado" NOT IN ('ANU')
		AND "IdEmpresa" = :IdEmpresa
) , evento_capital_cli_30 AS (
	SELECT
		SUM("CantidadDerecho") AS "CantidadDerecho", "IdPortafolio", EC."FechaPago" , ECLI."IdEventoCapital",
		SUM("MontoImpuestoExtranjero") AS "MontoImpuestoExtranjero", SUM("MontoGastoTransferencia") AS "MontoGastoTransferencia"
	FROM evento_capital_30 EC
	LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" ECLI ON EC."IdEventoCapital" = ECLI."IdEventoCapital"
	WHERE
		ECLI."IdPortafolio" IN (SELECT "IdPortafolio"  FROM #TempIdPortafolios)
	GROUP BY "IdPortafolio", EC."FechaPago" , ECLI."IdEventoCapital" 
), cartera_30 AS (
	SELECT
		CAR."IdCliente" ,
		CAR."IdPortafolio" ,
		CAR."IdMonedaSistema" ,
		CAR."IdInstrumento" ,
		CAR."IdEstado" ,
		SUM(CAR."INGCantidad") AS "INGCantidad"
	FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
	WHERE
		CAR."IdEstado" = 'VIG'
		AND CAR."IdCliente" IN (SELECT "IdCliente" from #TempIdClientes)
		AND CAR."IdPortafolio" IN (SELECT "IdPortafolio"  FROM #TempIdPortafolios)
		AND CAR."FechaRespaldo" = :FechaDesde
	GROUP BY
		CAR."IdCliente" ,
		CAR."IdPortafolio",
		CAR."IdMonedaSistema",
		CAR."IdInstrumento",
		CAR."IdEstado" 
), flujo_proyectado_30 AS (
	SELECT
		CAR."IdCliente" ,
		CAR."IdPortafolio" ,
		CAR."IdMonedaSistema" ,
		SUM((COALESCE(ECLI."CantidadDerecho" ,CAR."INGCantidad") * EC."Factor" * EC."Paridad") - COALESCE(ECLI."MontoImpuestoExtranjero",0) - COALESCE(ECLI."MontoGastoTransferencia",0)) AS "MontoMonedaSistema",
		EC."IdMonedaLiquidacion" ,
		EC."FechaPago" AS "FechaLiquidacion",
		'Dividendos' AS "Item",
		EC."Moneda",
		0 AS "MontoMonedaAdicional"	
	FROM evento_capital_30 EC
	INNER JOIN cartera_30 CAR ON EC."IdInstrumento" = CAR."IdInstrumento"
	LEFT JOIN evento_capital_cli_30 ECLI ON EC."IdEventoCapital" = ECLI."IdEventoCapital" AND ECLI."IdPortafolio" = CAR."IdPortafolio" 
	GROUP BY
		CAR."IdCliente" ,
		CAR."IdPortafolio",
		EC."FechaPago",
		EC."Moneda",
		CAR."IdMonedaSistema",
		EC."IdMonedaLiquidacion"
), dolar_observado AS (
	SELECT
		PF."Valor",
		PF."Fecha",
		PF."IdParametroFinanciero", 
		ROW_NUMBER() OVER (PARTITION BY PF."IdParametroFinanciero" ORDER BY pf."Fecha" DESC) AS "row_num"
	FROM
		"BSCL.Plataforma.BD::MER.ParametroFinanciero" PF
	WHERE 
		PF."IdParametroFinanciero" = 'DO'
		AND "Fecha" <= :FechaDesde
), parametros_financieros AS (
	SELECT
		PF."Valor" * vDO."Valor" AS "Valor",
		PF."Fecha",
		PF."IdParametroFinanciero", 
		ROW_NUMBER() OVER (PARTITION BY PF."IdParametroFinanciero"
	ORDER BY
		pf."Fecha" DESC) AS "row_num"
	FROM
		"BSCL.Plataforma.BD::MER.ParametroFinanciero" PF, dolar_observado vDO
	WHERE 
		vDO."row_num" = 1 
		AND  PF."IdParametroFinanciero" IN ('EUR', 'NZD', 'GBP', 'PEN', 'COP', 'JPY')
		AND  PF."Fecha" <= :FechaDesde
) SELECT
	fp30."IdCliente" ,
	fp30."IdPortafolio" ,
	fp30."FechaLiquidacion", 
	fp30."Item",
	fp30."Moneda" AS "IdMonedaInstrumento",
	fp30."IdMonedaSistema",
	CASE 
		WHEN fp30."IdMonedaLiquidacion" IN ('EUR', 'NZD', 'GBP', 'PEN', 'COP', 'JPY') THEN fp30."MontoMonedaSistema" * COALESCE(pf."Valor", 1)
		WHEN fp30."IdMonedaLiquidacion" = 'USD' THEN fp30."MontoMonedaSistema" * COALESCE(vDO."Valor", 1)
		ELSE fp30."MontoMonedaSistema"
	END AS "MontoMonedaSistema",
	fp30."IdMonedaLiquidacion" AS "IdMonedaAdicional",
	CASE 
		WHEN fp30."IdMonedaLiquidacion" IN ('EUR', 'NZD', 'GBP', 'PEN', 'COP', 'JPY') THEN fp30."MontoMonedaSistema" * COALESCE(pf."Valor", 1)
		WHEN fp30."IdMonedaLiquidacion" = 'USD' THEN fp30."MontoMonedaSistema" * COALESCE(vDO."Valor", 1)
		ELSE fp30."MontoMonedaSistema"
	END AS "MontoMonedaAdicional"
	FROM
		flujo_proyectado_30 fp30
		LEFT JOIN parametros_financieros pf ON  fp30."IdMonedaLiquidacion" = pf."IdParametroFinanciero" AND pf."row_num" = 1, dolar_observado vDO
	WHERE vDO."row_num" = 1 ;