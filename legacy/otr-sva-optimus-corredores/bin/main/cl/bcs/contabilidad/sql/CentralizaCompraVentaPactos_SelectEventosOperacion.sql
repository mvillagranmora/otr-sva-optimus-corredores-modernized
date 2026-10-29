WITH v_TabEventosPaso_OPERACIONES AS (
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion"
	,ConsultarCartera."IdCartera" AS "IdMovimiento"
	,Operacion."MontoOperacionInicial"
	,Operacion."MontoOperacionInicialMoneda"
	,ConsultarCartera."IdMonedaSistema"
	,ConsultarCartera."IdMonedaInstrumento"
	FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
	INNER JOIN "BSCL.Mesa.BD::MES.Operacion" Operacion ON (Operacion."FolioCartera" = ConsultarCartera."FolioCartera")
	WHERE 
		ConsultarCartera."IdEstado" 	IN ('VIG','VCD','ANT') AND 
		ConsultarCartera."FechaOperacion" 	= :FechaContable
)
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,'MONTO_INICIAL_PACTO' 	AS "IdEvento" ,'Monto Inicial Pacto' 	AS "Nombre" ,"MontoOperacionInicial" 					AS "MontoEvento","IdMonedaSistema" 		AS "CodigoMoneda" 	FROM v_TabEventosPaso_OPERACIONES 	UNION
SELECT "IdMovimiento" AS "IdMovimiento","IdTipoCentralizacion" ,'VALOR_INICIAL_PACTO' 	AS "IdEvento" ,'Valor Inicial Pacto' 	AS "Nombre" ,"MontoOperacionInicialMoneda" 				AS "MontoEvento","IdMonedaInstrumento" 	AS "CodigoMoneda" 	FROM v_TabEventosPaso_OPERACIONES;