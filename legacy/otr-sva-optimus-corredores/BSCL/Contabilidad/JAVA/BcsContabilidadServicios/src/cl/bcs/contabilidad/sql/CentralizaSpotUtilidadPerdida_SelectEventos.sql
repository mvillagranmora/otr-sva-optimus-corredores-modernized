WITH TabEventosPaso AS(
	SELECT DISTINCT
			"IdOperacionUI"								AS "IdMovimiento"
			,TO_BIGINT(:IdTipoCentralizacion) 			AS "IdTipoCentralizacion"
			,"IdTipoOperacion"							AS "IdTipoOperacion"
			,"MontoMonedaPrincipal"
			,"MontoMonedaSecundaria"
			,"MontoMonedaPrincipalEquivalente"
			,"MontoEquivalente"							AS "MontoMonedaSecundariaEquivalente"
			,"IdMonedaPrincipal"
			,"IdMonedaSecundaria"
			,"TipoCambioCosto"
			,"ResultadoMonedaLocalBidAsk"
			,"ResultadoMonedaLocalDO"
			,"ResultadoMonedaLocalPuntas"
			,"ResultadoMonedaLocalTCCierreMesa"
			,"ResultadoMonedaLocalTCSpot"
			,SPOT."NombreInstrumento"	AS "Nemotecnico"
			,CASE WHEN TRIM(SPOT."IdTipoOperacion") = 'COMSPT' AND SPOT."Margen" > 0 THEN SPOT."Margen"
		   		  WHEN TRIM(SPOT."IdTipoOperacion") = 'VENSPT' AND SPOT."Margen" > 0 THEN SPOT."Margen"
				  ELSE 0 END AS "Utilidad"
			,CASE WHEN TRIM(SPOT."IdTipoOperacion") = 'COMSPT' AND SPOT."Margen" < 0 THEN SPOT."Margen"
				  WHEN TRIM(SPOT."IdTipoOperacion") = 'VENSPT' AND SPOT."Margen" < 0 THEN SPOT."Margen"
				  ELSE 0 END AS "Perdida"
			,TO_DECIMAL(0,26,8) AS "Moposi"
		FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" SPOT
		WHERE SPOT."IdEmpresa" = :IdEmpresa 
		AND LEFT(SPOT."FechaOperacion",10) = :FechaContable 
        AND SPOT."Estado" = 'VIG'
        UNION ALL
        SELECT TOP 1
        	
			"IdResultadoContableSpot"				AS "IdMovimiento"
			,TO_BIGINT(:IdTipoCentralizacion) 			AS "IdTipoCentralizacion"
			,''										AS "IdTipoOperacion"
			,0
			,0
			,0
			,0										AS "MontoMonedaSecundariaEquivalente"
			,:MonedaSistema
			,:MonedaSistema
			,TO_DECIMAL(0,26,8) AS "TipoCambioCosto"
			,0
			,0
			,0
			,0
			,0
			,''										AS "Nemotecnico"
			,0 										AS "Utilidad"
			,0 										AS "Perdida"
			,"UtilidadPerdidaDolObs" AS "Moposi"
		FROM "BSCL.Mesa.BD::SPT.ResultadoContableSpot"
	   WHERE "IdEstado" = 'ACT' 
	   AND "Fecha" = :FechaContable
)
	   
	   
SELECT DISTINCT
	 "IdMovimiento" 						AS "IdMovimiento"
	,"IdTipoCentralizacion"
	,CASE TRIM("IdTipoOperacion")
	 WHEN 'COMSPT' THEN 'MONEDA_PRINCIPAL_SPOT_COMPRA'
	 ELSE 'MONEDA_PRINCIPAL_SPOT_VENTA'			
	 END						 			AS "IdEvento"
	,'Valor Moneda Principal Spot Compra' 	AS "Nombre"
	,"MontoMonedaPrincipal" 				AS "MontoEvento"
	,"IdMonedaPrincipal" 					AS "CodigoMoneda"
FROM TabEventosPaso
--UNION ALL
--SELECT DISTINCT
--	 "IdMovimiento"
--	,"IdTipoCentralizacion"
--	,CASE TRIM("IdTipoOperacion") WHEN 'COMSPT' THEN 'MONEDA_PRINCIPAL_SPOT_COMPRA' ELSE 'MONEDA_PRINCIPAL_SPOT_VENTA' END
--	,'Valor a Moneda Princiap'
--	,"MontoMonedaPrincipal"
--	,"IdMonedaPrincipal"
--FROM TabEventosPaso
UNION ALL
SELECT DISTINCT
	 "IdMovimiento"
	,"IdTipoCentralizacion"
	,CASE TRIM("IdTipoOperacion") WHEN 'COMSPT' THEN 'MONEDA_SECUNDARIA_SPOT_COMPRA' ELSE 'MONEDA_SECUNDARIA_SPOT_VENTA' END
	,'Valor a Moneda Secundaria'
	,"MontoMonedaSecundaria"
	,"IdMonedaSecundaria"
FROM TabEventosPaso
UNION ALL
SELECT DISTINCT
	 "IdMovimiento"
	,"IdTipoCentralizacion"
	,CASE TRIM("IdTipoOperacion") WHEN 'COMSPT' THEN 'MONEDA_PRINCIPAL_EQUIVALENTE_SPOT_COMPRA' ELSE 'MONEDA_PRINCIPAL_EQUIVALENTE_SPOT_VENTA' END
	,'Valor Moneda Principal Equivalente'
	,"MontoMonedaPrincipalEquivalente"
	,"IdMonedaPrincipal"
FROM TabEventosPaso
UNION ALL
SELECT DISTINCT
	 "IdMovimiento"
	,"IdTipoCentralizacion"
	,CASE TRIM("IdTipoOperacion") WHEN 'COMSPT' THEN 'MONEDA_SECUNDARIA_EQUIVALENTE_SPOTCOMPRA' ELSE 'MONEDA_SECUNDARIA_EQUIVALENTE_SPOT_VENTA' END
	,'Valor Moneda Secundaria Equivalente'
	,"MontoMonedaSecundariaEquivalente"
	,"IdMonedaSecundaria"
FROM TabEventosPaso
UNION ALL
SELECT DISTINCT
	 "IdMovimiento"
	,"IdTipoCentralizacion"	
	,'UTILIDAD'
	,'Utilidad'
	,"Utilidad"
	,"IdMonedaSecundaria"
FROM TabEventosPaso
UNION ALL
SELECT DISTINCT
	 "IdMovimiento"
	,"IdTipoCentralizacion"
	,'PERDIDA'
	,'Perdida'
	,"Perdida"
	,"IdMonedaSecundaria"
FROM TabEventosPaso
UNION ALL
SELECT DISTINCT
	 "IdMovimiento"
	,"IdTipoCentralizacion"		
	,'RESULTADO_MONEDA_LOCAL_BID_ASK'
	,'Resultado Moneda local BidAsk'
	,"ResultadoMonedaLocalBidAsk"
	,:MonedaSistema
FROM TabEventosPaso
UNION ALL
SELECT DISTINCT
	 "IdMovimiento"
	,"IdTipoCentralizacion"		
	,'RESULTADO_MONEDA_LOCAL_DO'
	,'Resultado Moneda Local Dolar Obs.'
	,"ResultadoMonedaLocalDO"
	,:MonedaSistema
FROM TabEventosPaso
UNION ALL
SELECT DISTINCT
	 "IdMovimiento"
	,"IdTipoCentralizacion"		
	,'UTILIDAD_DO'
	,'Utilidad Operacion DO'
	,CASE WHEN "ResultadoMonedaLocalDO" > 0 THEN "ResultadoMonedaLocalDO" ELSE 0 END AS "MontoEvento"
	,:MonedaSistema
FROM TabEventosPaso
UNION ALL
SELECT DISTINCT
	 "IdMovimiento"
	,"IdTipoCentralizacion"		
	,'PERDIDA_DO'
	,'Perdida Operacion DO'
	,CASE WHEN "ResultadoMonedaLocalDO" < 0 THEN "ResultadoMonedaLocalDO" ELSE 0 END AS "MontoEvento"
	,:MonedaSistema
FROM TabEventosPaso
UNION ALL
SELECT DISTINCT
	 "IdMovimiento"
	,"IdTipoCentralizacion"		
	,'RESULTADO_MONEDA_LOCAL_PUNTAS'
	,'Resultado Moneda Local Puntas'
	,"ResultadoMonedaLocalPuntas"
	,:MonedaSistema
FROM TabEventosPaso
UNION ALL
SELECT DISTINCT
	 "IdMovimiento"
	,"IdTipoCentralizacion"		
	,'RESULTADO_MONEDA_LOCAL_TC_CIERRE_MESA'
	,'Resultado Moneda Local T/C Cierre Mesa'
	,"ResultadoMonedaLocalTCCierreMesa"
	,:MonedaSistema
FROM TabEventosPaso
UNION ALL
SELECT DISTINCT
	 "IdMovimiento"
	,"IdTipoCentralizacion"		
	,'RESULTADO_MONEDA_LOCAL_TC_SPOT'
	,'Resultado Moneda Local T/C Spot'
	,"ResultadoMonedaLocalTCSpot"
	,:MonedaSistema
FROM TabEventosPaso
UNION ALL
SELECT DISTINCT
	 "IdMovimiento"
	,"IdTipoCentralizacion"		
	,'UTILIDAD_MOPOSI'
	,'Utilidad MOPOSI'
	,CASE WHEN "Moposi" > 0 THEN "Moposi" ELSE TO_DECIMAL(0,26,8) END AS "Moposi"
	,:MonedaSistema
FROM TabEventosPaso
UNION ALL
SELECT DISTINCT
	 "IdMovimiento"
	,"IdTipoCentralizacion"		
	,'PERDIDA_MOPOSI'
	,'Perdida MOPOSI'
	,CASE WHEN "Moposi" < 0 THEN "Moposi" ELSE TO_DECIMAL(0,26,8) END AS "Moposi"
	,:MonedaSistema
FROM TabEventosPaso
UNION ALL
SELECT DISTINCT
	 "IdMovimiento"
	,"IdTipoCentralizacion"		
	,'MONTO_TC_COSTO'
	,'Monto T/C Costo Spot'
	,CASE 
		WHEN TRIM("IdTipoOperacion") = 'COMSPT' THEN TO_DECIMAL(ROUND("MontoMonedaSecundaria" + "Perdida" + "Utilidad",0),26,8) 
		WHEN TRIM("IdTipoOperacion") = 'VENSPT' THEN TO_DECIMAL(ROUND("MontoMonedaSecundaria" - "Perdida" - "Utilidad",0),26,8) 
	END AS "MontoEvento"
	,:MonedaSistema
FROM TabEventosPaso AS Operacion
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON
ON (Operacion."IdMonedaPrincipal" = MON."IdMoneda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMoneda" AS TIPMON
ON (MON."IdTipoMoneda" = TIPMON."IdTipoMoneda");
