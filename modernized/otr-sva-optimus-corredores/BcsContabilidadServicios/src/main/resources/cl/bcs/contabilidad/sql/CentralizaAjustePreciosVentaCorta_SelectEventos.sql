WITH v_TabEventosPaso AS(
SELECT    
     "IdCartera"                        			AS "IdMovimiento"
    ,TO_BIGINT(:IdTipoCentralizacion)   			AS "IdTipoCentralizacion"
    ,"INGMontoMercadoInstrumento" 					AS "INGMontoATasaMercadoActual"
				,"INGMontoATasaMercadoProximo"
				,"INGMontoATasaMercadoAnterior"				
				,"INGMontoInstrumento" 				AS "INGMontoATasaCompraActual"
				,"INGMontoATasaCompraAnterior"
				,"INGMontoPrecioPromedioCompra"
			  	,CASE WHEN ("INGMontoMercadoInstrumento" -"INGMontoATasaMercadoAnterior") > 0 THEN ("INGMontoMercadoInstrumento" - "INGMontoATasaMercadoAnterior")
					ELSE 0
					END 											AS "MayorValorMercado"
				,CASE WHEN ("INGMontoMercadoInstrumento" - "INGMontoATasaMercadoAnterior") < 0 THEN ABS(("INGMontoMercadoInstrumento" - "INGMontoATasaMercadoAnterior"))
					ELSE 0
					END												AS "MenorValorMercado"				
				,CASE WHEN ("INGMontoMercadoInstrumento" - "INGMontoPrecioPromedioCompra") > 0 THEN ("INGMontoMercadoInstrumento" - "INGMontoPrecioPromedioCompra")
					ELSE 0
					END												AS "MayorValorPrecPromCompra"
				,CASE WHEN ("INGMontoMercadoInstrumento" - "INGMontoPrecioPromedioCompra") < 0 THEN ABS(("INGMontoMercadoInstrumento" - "INGMontoPrecioPromedioCompra"))
					ELSE 0
					END 											AS "MenorValorPrecPromCompra"
				,CASE WHEN ("INGMontoMercadoInstrumento" - "INGMontoInstrumento") > 0 THEN ("INGMontoMercadoInstrumento" - "INGMontoInstrumento")
					ELSE 0
					END 											AS "MayorValorPrecioCompra"
				,CASE WHEN ("INGMontoMercadoInstrumento" - "INGMontoInstrumento") < 0 THEN ABS(("INGMontoMercadoInstrumento" - "INGMontoInstrumento"))
					ELSE 0
					END 											AS "MenorValorPrecioCompra"			
				,"IdMonedaSubyacente" AS "IdMonedaInstrumento"
				,"TasaPrestamo"       AS "Tasa"
		FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" ConsultarPrestamo
		WHERE "IdEmpresa" = :IdEmpresa
			AND "INGFechaOperacion" <= :FechaContable 
			AND "EGRCantidadPrestamo" = 0
	        AND "IdClasificacionTipoInstrumento" = 'RV'
	        AND "IdTipoInstrumento" IN ('PRT', 'EMP')
--	        AND "DocumentoIdentidad" = :DocumentoIdentidad
	        AND "IdEstado" = 'VIG' 
) 

SELECT   
	 "IdMovimiento"                         AS "IdMovimiento"
    ,"IdTipoCentralizacion"
    ,'VALOR_ACTUAL_MERCADO_RV'  			AS "IdEvento"
    ,'Valor Mercado Actual RV'           	AS "Nombre"
    ,"INGMontoATasaMercadoActual"           AS "MontoEvento"
    ,"IdMonedaInstrumento"                  AS "CodigoMoneda"
FROM v_TabEventosPaso
UNION  
SELECT "IdMovimiento", "IdTipoCentralizacion",'VALOR_ANTERIOR_MERCADO_RV'		,'Valor Mercado Anterior RV'		,"INGMontoATasaMercadoAnterior"   ,"IdMonedaInstrumento" FROM v_TabEventosPaso UNION 
SELECT "IdMovimiento", "IdTipoCentralizacion",'VALOR_ACTUAL_COMPRA_RV'			,'Valor Compra Actual RV'			,"INGMontoATasaCompraActual"      ,"IdMonedaInstrumento" FROM v_TabEventosPaso UNION 
SELECT "IdMovimiento", "IdTipoCentralizacion",'VALOR_ANTERIOR_COMPRA_RV'		,'Valor Compra Anterior RV'			,"INGMontoATasaCompraAnterior"    ,"IdMonedaInstrumento" FROM v_TabEventosPaso UNION 
SELECT "IdMovimiento", "IdTipoCentralizacion",'VALOR_PROMEDIO_COMPRA_RV' 		,'Valor Promedio Compra RV'			,"INGMontoPrecioPromedioCompra"   ,"IdMonedaInstrumento" FROM v_TabEventosPaso UNION 
SELECT "IdMovimiento", "IdTipoCentralizacion",'MAYOR_VALOR_MERCADO_RV'			,'Mayor Valor Mercado RV'			,"MayorValorMercado"   			  ,"IdMonedaInstrumento" FROM v_TabEventosPaso UNION 
SELECT "IdMovimiento", "IdTipoCentralizacion",'MENOR_VALOR_MERCADO_RV'			,'Menor Valor Mercado RV'			,"MenorValorMercado"   			  ,"IdMonedaInstrumento" FROM v_TabEventosPaso UNION 
SELECT "IdMovimiento", "IdTipoCentralizacion",'MAYOR_VALOR_PROMEDIO_COMPRA_RV'	,'Mayor Valor Promedio Compra RV'	,"MayorValorPrecPromCompra"   	  ,"IdMonedaInstrumento" FROM v_TabEventosPaso UNION 
SELECT "IdMovimiento", "IdTipoCentralizacion",'MENOR_VALOR_PROMEDIO_COMPRA_RV'	,'Menor Valor Promedio Compra RV'	,"MenorValorPrecPromCompra"   	  ,"IdMonedaInstrumento" FROM v_TabEventosPaso UNION 
SELECT "IdMovimiento", "IdTipoCentralizacion",'MAYOR_VALOR_COMPRA_RV'			,'Mayor Valor Compra RV'			,"MayorValorPrecioCompra"   	  ,"IdMonedaInstrumento" FROM v_TabEventosPaso UNION 
SELECT "IdMovimiento", "IdTipoCentralizacion",'MENOR_VALOR_COMPRA_RV'			,'Menor Valor Compra RV'			,"MenorValorPrecioCompra"   	  ,"IdMonedaInstrumento" FROM v_TabEventosPaso UNION 
SELECT "IdMovimiento", "IdTipoCentralizacion",'TASA'							,'Tasa'								,"Tasa"   	 					  ,"IdMonedaInstrumento" FROM v_TabEventosPaso; 
