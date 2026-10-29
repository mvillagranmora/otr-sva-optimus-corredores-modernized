SELECT 
"IdTipoOperacion"
,"Nemotecnico"
,"IdTipoCanalIngreso"
,"IdMonedaPrincipal"
,"IdMonedaSecundaria"
,"IdMonedaEquivalente"
,"IdMonedaLocal"
,"MontoMonedaPrincipal"
,"MontoMonedaSecundaria"
,TO_DECIMAL(("MontoMonedaPrincipal"*"TipoCambioCosto"),26,8) AS "MontoMonedaSecundariaTCCosto"
,"ResultadoMonedaLocalConsolidado"
,"MontoMonedaPrincipalEquivalente"
,"ResultadoMonedaLocalPuntas"
,"ResultadoMonedaLocalBidAsk"
,"ResultadoMonedaLocalDO"
,"ResultadoMonedaLocalTCSpot"
,"ResultadoMonedaLocalTCCierreMesa"
,"TipoCambioCierre"
,"TipoCambioCosto"
,CASE WHEN "MontoMonedaPrincipal">0 THEN "MontoMonedaPrincipalEquivalente"/"MontoMonedaPrincipal" ELSE 0 END AS "ParidadMercado"
,"ParidadBid"
,"ParidadAsk"
,"Margen"
,"Estado"
,"MultiplicaODivide"
, TO_VARCHAR("FechaRecibimos",'YYYY-MM-DD') AS "FechaValutaRecibimos"
, TO_VARCHAR("FechaEntregamos",'YYYY-MM-DD') AS "FechaValutaEntregamos"
,FormaPagoRec."IdMonedaComprobantePago" AS "IdMonedaRecibimos"
,FormaPagoEnt."IdMonedaComprobantePago" AS "IdMonedaEntregamos"
FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" SPT
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON (SPT."IdMonedaPrincipal" = MON."IdMoneda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMoneda" AS TIPMON ON (MON."IdTipoMoneda" = TIPMON."IdTipoMoneda")
LEFT JOIN (
	SELECT
		DISTINCT "IdOperacion",
		"IdMonedaComprobantePago" ,
		"IdEmpresa"
	FROM
		"BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion"
	WHERE
		"IdTipoProducto" = 'MEME'
		AND "RecibePaga" = 'P' ) AS FormaPagoEnt ON
	(FormaPagoEnt."IdOperacion" = SPT."IdOperacion" 
		AND FormaPagoEnt."IdEmpresa" = SPT."IdEmpresa")
LEFT JOIN (
	SELECT
		DISTINCT "IdOperacion",
		"IdMonedaComprobantePago" ,
		"IdEmpresa"
	FROM
		"BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion"
	WHERE
		"IdTipoProducto" = 'MEME'
		AND "RecibePaga" = 'R' ) AS FormaPagoRec ON
	(FormaPagoRec."IdOperacion" = SPT."IdOperacion" 
		AND FormaPagoRec."IdEmpresa" = SPT."IdEmpresa")
WHERE TO_DATE("FechaOperacion") = :FechaOperacion
AND "IdMonedaPrincipal" = :MonedaPrincipal


