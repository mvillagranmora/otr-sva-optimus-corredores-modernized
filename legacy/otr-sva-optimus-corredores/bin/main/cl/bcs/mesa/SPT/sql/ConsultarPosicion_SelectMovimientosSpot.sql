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
,CASE "IdTipoOperacion" WHEN 'COMSPT' THEN TO_VARCHAR("FechaRecibimos",'YYYY-MM-DD') ELSE TO_VARCHAR("FechaEntregamos",'YYYY-MM-DD') END AS "FechaValuta"
,"OperacionFicticia" as "OperacionNoFacturable"
FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" SPT
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON (SPT."IdMonedaPrincipal" = MON."IdMoneda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMoneda" AS TIPMON ON (MON."IdTipoMoneda" = TIPMON."IdTipoMoneda")
WHERE TO_DATE("FechaOperacion") = :FechaOperacion
AND "IdMonedaPrincipal" = :MonedaPrincipal