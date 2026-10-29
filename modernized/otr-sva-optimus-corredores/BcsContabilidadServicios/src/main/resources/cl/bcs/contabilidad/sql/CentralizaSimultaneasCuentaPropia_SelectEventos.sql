--IdTipoCentralizacion = 460: CodigoCentralizacion = 14; Informe = Simultanea
WITH v_TabEventosPaso AS (
    SELECT
        TO_BIGINT(:p_IdTipoCentralizacion)    	AS "IdTipoCentralizacion"
        ,CVIFRF."IdValorizacion"                   	AS "IdMovimiento"    
        ,ROUND(CVIFRF."MontoCompra",0)		  	AS "MontoCompra"
        ,ROUND(CVIFRF."MontoActual",0) 		  	AS "MontoActual"                        
        ,ROUND(CVIFRF."MontoProximo",0)		  	AS "MontoProximo"
        ,ROUND(CVIFRF."MontoInteres",0)			AS "MontoInteres"
        ,ROUND(CVIFRF."MontoInteresCorrido",0)	AS "MontoInteresCorrido"
        ,CVIFRF."IdMonedaInstrumento"
        ,CVIFRF."IdTipoOperacion"
        ,CVIFRF."Nemotecnico"
    FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" CVIFRF
    WHERE CVIFRF."IdEmpresa" = :p_IdEmpresa
	  AND CVIFRF."IdInforme" = :p_IdInforme
)
SELECT "IdMovimiento","IdTipoCentralizacion",CASE "IdTipoOperacion" WHEN 'COMSIM' THEN 'MONTO_COMPRA_SIMULTANEA_COMPRA' ELSE 'MONTO_COMPRA_SIMULTANEA_VENTA' END  AS "IdEvento",'Valor a Tasa Mercado Actual' AS "Nombre","MontoCompra"  AS "MontoEvento","IdMonedaInstrumento" AS "CodigoMoneda" FROM v_TabEventosPaso
UNION ALL
SELECT "IdMovimiento","IdTipoCentralizacion",CASE "IdTipoOperacion" WHEN 'COMSIM' THEN 'MONTO_ACTUAL_SIMULTANEA_COMPRA' ELSE 'MONTO_ACTUAL_SIMULTANEA_VENTA' END  AS "IdEvento",'Valor a Tasa Mercado Actual' AS "Nombre","MontoActual"  AS "MontoEvento","IdMonedaInstrumento" AS "CodigoMoneda" FROM v_TabEventosPaso
UNION ALL
SELECT "IdMovimiento","IdTipoCentralizacion",CASE "IdTipoOperacion" WHEN 'COMSIM' THEN 'MONTO_PROXIMO_SIMULTANEA_COMPRA' ELSE 'MONTO_PROXIMO_SIMULTANEA_VENTA' END AS "IdEvento",'Valor a Tasa Mercado Proximo' AS "Nombre","MontoProximo" AS "MontoEvento","IdMonedaInstrumento" AS "CodigoMoneda" FROM v_TabEventosPaso
UNION ALL
SELECT "IdMovimiento","IdTipoCentralizacion",CASE "IdTipoOperacion" WHEN 'COMSIM' THEN 'INTERES_DIARIO_SIMULTANEA_COMPRA' ELSE 'INTERES_DIARIO_SIMULTANEA_VENTA' END AS "IdEvento",'Intereses simultanea' AS "Nombre", "MontoInteres" AS "MontoEvento","IdMonedaInstrumento" AS "CodigoMoneda" FROM v_TabEventosPaso
UNION ALL
SELECT "IdMovimiento","IdTipoCentralizacion",CASE "IdTipoOperacion" WHEN 'COMSIM' THEN 'INTERES_ACUMULADO_SIMULTANEA_COMPRA' ELSE 'INTERES_ACUMULADO_SIMULTANEA_VENTA' END AS "IdEvento",'Intereses simultanea' AS "Nombre", "MontoInteresCorrido" AS "MontoEvento","IdMonedaInstrumento" AS "CodigoMoneda" FROM v_TabEventosPaso;