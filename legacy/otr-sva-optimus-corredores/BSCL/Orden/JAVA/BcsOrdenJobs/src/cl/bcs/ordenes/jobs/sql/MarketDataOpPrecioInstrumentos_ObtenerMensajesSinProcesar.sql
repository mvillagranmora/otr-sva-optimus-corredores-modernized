/********************************************************** 
Proposito	: Script para obtener mensajes de precio mercado sin procesar
autor		: projo
fecha		: 12/03/2020
***********************************************************/
SELECT ins."IdInstrumento",ins."IdTipoInstrumento",insop."codTicker",insop."timeTicker",insop."codIsin",insop."action",insop."instruments",COALESCE(insop."imbalance",0) AS "imbalance",COALESCE(insop."tradeVolume",0) AS "tradeVolume"
	,COALESCE(insop."avgPrice",0) AS "avgPrice",COALESCE(insop."tradeAmount",0) AS "tradeAmount",insop."trend",COALESCE(insop."cfiCode",'') AS "cfiCode",insop."maturityDate",insop."issuer",COALESCE(insop."eventType",0) AS "eventType"
	,COALESCE(insop."settlement",'') AS "settlement",COALESCE(insop."exercise",'') AS "exercise",COALESCE(insop."openContract",'') AS "openContract",COALESCE(insop."StrikePrice",0) AS "StrikePrice"
	,COALESCE(insop."minPrice",0) AS "minPrice",COALESCE(insop."highPrice",0) AS "highPrice"
FROM "BSCL.Orden.BD::MOP.INSTRUMENTOS_OP" insop 
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."Nemotecnico" = insop."instruments" 
ORDER BY insop."instruments"
