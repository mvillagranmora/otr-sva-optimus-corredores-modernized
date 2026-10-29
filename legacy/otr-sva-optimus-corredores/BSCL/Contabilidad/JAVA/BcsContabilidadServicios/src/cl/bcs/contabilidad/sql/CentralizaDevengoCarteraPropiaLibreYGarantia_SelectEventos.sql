--IdTipoCentralizacion = 450: CodigoCentralizacion = 08; Informe = Propia a Tasa Compra
WITH v_TabEventosPaso AS(
	SELECT
		CVIFRF."IdCartera" 						AS "IdMovimiento"
		,TO_BIGINT(:p_IdTipoCentralizacion)		AS "IdTipoCentralizacion"
		,ROUND(CVIFRF."MontoPactado",0)	AS "MontoPactado"
		,ROUND(CVIFRF."MontoCompra",0)  AS "MontoCompra"
        ,ROUND(CVIFRF."MontoActual",0) AS "MontoActual"                        
        ,ROUND(CVIFRF."MontoProximo",0) AS "MontoProximo"
        ,ROUND(CVIFRF."MontoInteres",0)	AS "MontoInteres"
        ,ROUND(CVIFRF."MontoInteresCorrido",0) AS "MontoInteresCorrido"
		,ROUND(CVIFRF."MontoReajuste",0) AS "MontoReajuste"
        ,CVIFRF."IdMonedaInstrumento"
	FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" CVIFRF 
	WHERE CVIFRF."IdInforme" = :p_IdInforme
	AND CVIFRF."IdEmpresa" = :p_IdEmpresa
	AND CVIFRF."IdClasificacionTipoInstrumento" in ('RF','IF')
)
SELECT "IdMovimiento","IdTipoCentralizacion",'MONTO_COMPRA' AS "IdEvento",'Monto Compra' AS "Nombre","MontoCompra" AS "MontoEvento","IdMonedaInstrumento" AS "CodigoMoneda" FROM v_TabEventosPaso
UNION ALL
SELECT "IdMovimiento", "IdTipoCentralizacion", 'MONTO_ACTUAL_TASA_COMPRA' AS "IdEvento", 'Monto Actual a Tasa Compra' AS "Nombre", "MontoActual" AS "MontoEvento", "IdMonedaInstrumento" AS "CodigoMoneda" FROM v_TabEventosPaso
UNION ALL 
SELECT "IdMovimiento", "IdTipoCentralizacion", 'MONTO_PROXIMO_TASA_COMPRA' AS "IdEvento", 'Monto Proximo a Tasa Compra' AS "Nombre", "MontoProximo" AS "MontoEvento", "IdMonedaInstrumento" AS "CodigoMoneda" FROM v_TabEventosPaso
UNION ALL 
SELECT "IdMovimiento", "IdTipoCentralizacion", 'INTERES_DIARIO_TASA_COMPRA' AS "IdEvento", 'Interes Diario a Tasa Compra' AS "Nombre", "MontoInteres" AS "MontoEvento", "IdMonedaInstrumento" AS "CodigoMoneda" FROM v_TabEventosPaso
WHERE "MontoInteres" >= 0
UNION ALL 
SELECT "IdMovimiento", "IdTipoCentralizacion", 'INTERES_NEGATIVO_DIARIO_TASA_COMPRA' AS "IdEvento", 'Interes Diario a Tasa Compra' AS "Nombre", "MontoInteres" AS "MontoEvento", "IdMonedaInstrumento" AS "CodigoMoneda" FROM v_TabEventosPaso
WHERE "MontoInteres" < 0
UNION ALL 
SELECT "IdMovimiento", "IdTipoCentralizacion", 'REAJUSTE_TASA_COMPRA' AS "IdEvento", 'Reajuste a Tasa Compra' AS "Nombre", "MontoReajuste" AS "MontoEvento", "IdMonedaInstrumento" AS "CodigoMoneda" FROM v_TabEventosPaso
WHERE "MontoReajuste" >= 0
UNION ALL 
SELECT "IdMovimiento", "IdTipoCentralizacion", 'REAJUSTE_NEGATIVO_TASA_COMPRA' AS "IdEvento", 'Reajuste a Tasa Compra' AS "Nombre", "MontoReajuste" AS "MontoEvento", "IdMonedaInstrumento" AS "CodigoMoneda" FROM v_TabEventosPaso
WHERE "MontoReajuste" < 0;