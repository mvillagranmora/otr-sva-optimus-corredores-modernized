--IdTipoCentralizacion = 430: CodigoCentralizacion = 05; Informe = Pacto a Tasa Pacto
WITH v_TabEventosPaso AS (
	SELECT
		TO_BIGINT(:p_IdTipoCentralizacion) 	AS "IdTipoCentralizacion"
		,CVIFRF."IdValorizacion"					AS  "IdMovimiento"
		,CVIFRF."Nemotecnico"
		,ROUND(CVIFRF."MontoCompra",0)		  	AS "MontoCompra"
		,ROUND(CVIFRF."MontoPactado",0)			AS "MontoPactado"
        ,ROUND(CVIFRF."MontoActual",0) 		  	AS "MontoActual"                        
        ,ROUND(CVIFRF."MontoProximo",0)		  	AS "MontoProximo"
        ,ROUND(CVIFRF."MontoInteres",0)			AS "MontoInteres"
        ,ROUND(CVIFRF."MontoInteresCorrido",0)	AS "MontoInteresCorrido"
		,ROUND(CVIFRF."MontoReajuste",0)		AS "MontoReajuste"
		,CVIFRF."IdMonedaInstrumento"
		,CVIFRF."IdTipoOperacion"
	FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" CVIFRF 
	WHERE CVIFRF."IdInforme" = :p_IdInforme
	AND CVIFRF."IdEmpresa" = :p_IdEmpresa
    AND CVIFRF."IdTipoOperacion" in ('COMPAC' , 'VENPAC')
)
SELECT  "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion", 'MONTO_OPERACION' AS "IdEvento",'Monto Operacion' AS "Nombre", "MontoCompra" AS "MontoEvento","IdMonedaInstrumento" AS "CodigoMoneda" FROM v_TabEventosPaso 
UNION ALL 
SELECT  "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion", 'MONTO_PACTADO' AS "IdEvento",'Monto Pactado' AS "Nombre", "MontoPactado" AS "MontoEvento","IdMonedaInstrumento" AS "CodigoMoneda" FROM v_TabEventosPaso 
UNION ALL 
SELECT  "IdMovimiento","IdTipoCentralizacion", CASE "IdTipoOperacion" WHEN 'VENPAC' THEN 'MONTO_ANTERIOR_TP_PACTO_VENTA'	ELSE 'MONTO_ANTERIOR_TP_PACTO_COMPRA' END,'Monto Anterior a Tasa Pacto',"MontoActual","IdMonedaInstrumento" FROM v_TabEventosPaso 
UNION ALL 
SELECT "IdMovimiento", "IdTipoCentralizacion", CASE "IdTipoOperacion" WHEN 'VENPAC' THEN 'MONTO_PRESENTE_TP_PACTO_VENTA' ELSE 'MONTO_PRESENTE_TP_PACTO_COMPRA' END, 'Monto Presente a Tasa Pacto', "MontoProximo", "IdMonedaInstrumento" FROM v_TabEventosPaso 
UNION ALL 
SELECT "IdMovimiento", "IdTipoCentralizacion", CASE "IdTipoOperacion" WHEN 'VENPAC' THEN 'INTERES_DIARIO_TP_PACTO_VENTA' ELSE 'INTERES_DIARIO_TP_PACTO_COMPRA' END, 'Interes Diario', "MontoInteres", "IdMonedaInstrumento" FROM v_TabEventosPaso 
UNION ALL 
SELECT "IdMovimiento", "IdTipoCentralizacion", CASE "IdTipoOperacion" WHEN 'VENPAC' THEN 'INTERES_ACUMULADO_TP_PACTO_VENTA' ELSE 'INTERES_ACUMULADO_TP_PACTO_COMPRA' END, 'Interes Acumulado', "MontoInteresCorrido", "IdMonedaInstrumento" FROM v_TabEventosPaso 
UNION ALL 
SELECT "IdMovimiento", "IdTipoCentralizacion", CASE "IdTipoOperacion" WHEN 'VENPAC' THEN 'REAJUSTE_TP_PACTO_VENTA' 	 	ELSE 'REAJUSTE_TP_PACTO_COMPRA' END , 'Reajuste', "MontoReajuste", "IdMonedaInstrumento" FROM v_TabEventosPaso;
