WITH v_TabEventosPaso_OPERACIONES AS (
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion"
	,ConsultarCartera."IdCartera" AS "IdMovimiento"
	,Operacion."MontoOperacionFinalAnticipo"
	,Operacion."MontoOperacionInicial"
	,Operacion."MontoOperacionFinalMonedaAnticipo"
	,Operacion."MontoOperacionInicialMoneda"
	,(Operacion."MontoOperacionFinalAnticipo" - Operacion."MontoOperacionInicial") AS "Diferencia"
	,(Operacion."MontoOperacionFinalMonedaAnticipo" - Operacion."MontoOperacionInicialMoneda") AS "ValorDiferencia"
	,ConsultarCartera."IdMonedaSistema"
	,ConsultarCartera."IdMonedaInstrumento"
	,ABS(Operacion."MontoOperacionAnticipo") AS "MontoOperacionAnticipo"
	FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
	INNER JOIN "BSCL.Mesa.BD::MES.Operacion" Operacion ON (Operacion."FolioCartera" = ConsultarCartera."FolioCartera")
	WHERE ConsultarCartera."IdEmpresa" = :IdEmpresa 
    AND ConsultarCartera."EGRFechaOperacion" = :FechaContable                
    AND ConsultarCartera."IdTipoOperacion" in ('COMPAC','VENPAC')
    AND ConsultarCartera."IdEstado"         ='ANT'
    AND Operacion."IdEstado"         ='ANT'
)
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,'MONTO_INICIAL_PACTO' 	AS "IdEvento" ,'Monto Inicial Pacto' 	AS "Nombre" ,"MontoOperacionInicial" 					AS "MontoEvento","IdMonedaSistema" 		AS "CodigoMoneda" 	FROM v_TabEventosPaso_OPERACIONES 	UNION
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,'VALOR_INICIAL_PACTO' 	AS "IdEvento" ,'Valor Inicial Pacto' 	AS "Nombre" ,"MontoOperacionInicialMoneda" 				AS "MontoEvento","IdMonedaInstrumento" 	AS "CodigoMoneda" 	FROM v_TabEventosPaso_OPERACIONES   UNION
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,'MONTO_FINAL_PACTO' 	AS "IdEvento" ,'Monto Final Pacto' 		AS "Nombre" ,"MontoOperacionFinalAnticipo" 				AS "MontoEvento","IdMonedaSistema" 		AS "CodigoMoneda" 	FROM v_TabEventosPaso_OPERACIONES 	UNION
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,'VALOR_FINAL_PACTO' 	AS "IdEvento" ,'Valor Final Pacto' 		AS "Nombre" ,"MontoOperacionFinalMonedaAnticipo" 		AS "MontoEvento","IdMonedaInstrumento" 	AS "CodigoMoneda" 	FROM v_TabEventosPaso_OPERACIONES   UNION
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,'MONTO_DIFERENCIA' 		AS "IdEvento" ,'Monto Diferencia' 		AS "Nombre" ,"Diferencia" 								AS "MontoEvento","IdMonedaSistema" 		AS "CodigoMoneda" 	FROM v_TabEventosPaso_OPERACIONES 	UNION
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,'VALOR_DIFERENCIA' 		AS "IdEvento" ,'Valor Diferencia' 		AS "Nombre" ,"ValorDiferencia" 							AS "MontoEvento","IdMonedaInstrumento" 	AS "CodigoMoneda" 	FROM v_TabEventosPaso_OPERACIONES	UNION
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,'MONTO_ANTICIPO' 		AS "IdEvento" ,'Monto Anticipo' 		AS "Nombre" ,"MontoOperacionAnticipo"					AS "MontoEvento","IdMonedaInstrumento" 	AS "CodigoMoneda" 	FROM v_TabEventosPaso_OPERACIONES;

