-- Se borran los registros que ya existen con esas caractertisticas (despues se vuelven a insertar acualizados)
DELETE
FROM "BSCL.Plataforma.BD::INS.MarketMaker" AS MKTOPT
WHERE EXISTS 
(
	SELECT 1
	FROM "BSCL.Plataforma.BD::GBL.CargarMarketMakerDesdeBursatil_MarketMarket" AS MKTRUE
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON (INS."Nemotecnico" = MKTRUE."NEMO")
	WHERE MKTOPT."IdInstrumento" = INS."IdInstrumento"
	AND   MKTOPT."IdCorredora" 	 = MKTRUE."COD_CORR"
	AND MKTOPT."IdEstado"= (CASE WHEN  MKTRUE."ESTADO" = 'V' THEN 'ACT' ELSE 'INA' END )
);

