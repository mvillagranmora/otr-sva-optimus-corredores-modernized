SELECT 

	TRA."IdTransaccion",
	TRA."IdClasificacionTipoInstrumento",
	TRA."IdCondicionDeLiquidacion",
	TRA."IdMoneda",
	TRA."IdBolsa",
	TRA."IdTipoInstrumento",
	TRA."IndicadorOrdenDirecta",	
	TRA."IdComprobanteTesoreria",
	TRA."EsBilateral",
	
	TRA."IdCamara",
	BOL."EsExtranjera" AS "BolsaEsExtranjera"
	
FROM "BSCL.Orden.BD::ORD.Transaccion"  AS TRA
LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" AS BOL on BOL."IdBolsa" = TRA."IdBolsa"
WHERE TRA."IdTransaccion" IN (:IdTransaccion);