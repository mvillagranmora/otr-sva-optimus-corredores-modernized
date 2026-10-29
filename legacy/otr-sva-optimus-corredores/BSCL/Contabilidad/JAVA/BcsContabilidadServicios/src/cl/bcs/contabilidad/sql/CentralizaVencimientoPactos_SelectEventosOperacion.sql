WITH v_TabEventosPaso_OPERACIONES AS (
	SELECT TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion"
	,ConsultarCartera."IdCartera" AS "IdMovimiento"
	,Operacion."MontoOperacionFinal"
	,Operacion."MontoOperacionInicial"
	,Operacion."MontoOperacionFinalMoneda"
	,Operacion."MontoOperacionInicialMoneda"
	,(Operacion."MontoOperacionFinal" - Operacion."MontoOperacionInicial") AS "Diferencia"
	,(Operacion."MontoOperacionFinalMoneda" - Operacion."MontoOperacionInicialMoneda") AS "ValorDiferencia"
	,ConsultarCartera."IdMonedaSistema"
	,ConsultarCartera."IdMonedaInstrumento"
	FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
	INNER JOIN "BSCL.Mesa.BD::MES.Operacion" Operacion ON (Operacion."FolioCartera" = ConsultarCartera."FolioCartera")
	WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa 
    AND ConsultarCartera."FechaVencimientoOperacion" = :p_FechaContable              
    AND ConsultarCartera."IdTipoOperacion" in ('COMPAC','VENPAC')
    AND Operacion."IdEstado" = 'LIQ'
)
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,'MONTO_INICIAL_PACTO' 	AS "IdEvento" ,'Monto Inicial Pacto' 	AS "Nombre" ,"MontoOperacionInicial" 					AS "MontoEvento","IdMonedaSistema" 		AS "CodigoMoneda" 	FROM v_TabEventosPaso_OPERACIONES 	UNION
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,'VALOR_INICIAL_PACTO' 	AS "IdEvento" ,'Valor Inicial Pacto' 	AS "Nombre" ,"MontoOperacionInicialMoneda" 				AS "MontoEvento","IdMonedaInstrumento" 	AS "CodigoMoneda" 	FROM v_TabEventosPaso_OPERACIONES   UNION
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,'MONTO_FINAL_PACTO' 	AS "IdEvento" ,'Monto Final Pacto' 		AS "Nombre" ,"MontoOperacionFinal" 						AS "MontoEvento","IdMonedaSistema" 		AS "CodigoMoneda" 	FROM v_TabEventosPaso_OPERACIONES 	UNION
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,'VALOR_FINAL_PACTO' 	AS "IdEvento" ,'Valor Final Pacto' 		AS "Nombre" ,"MontoOperacionFinalMoneda" 				AS "MontoEvento","IdMonedaInstrumento" 	AS "CodigoMoneda" 	FROM v_TabEventosPaso_OPERACIONES   UNION
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,'MONTO_DIFERENCIA' 		AS "IdEvento" ,'Monto Diferencia' 		AS "Nombre" ,"Diferencia" 								AS "MontoEvento","IdMonedaSistema" 		AS "CodigoMoneda" 	FROM v_TabEventosPaso_OPERACIONES 	UNION
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,'VALOR_DIFERENCIA' 		AS "IdEvento" ,'Valor Diferencia' 		AS "Nombre" ,"ValorDiferencia" 							AS "MontoEvento","IdMonedaInstrumento" 	AS "CodigoMoneda" 	FROM v_TabEventosPaso_OPERACIONES;