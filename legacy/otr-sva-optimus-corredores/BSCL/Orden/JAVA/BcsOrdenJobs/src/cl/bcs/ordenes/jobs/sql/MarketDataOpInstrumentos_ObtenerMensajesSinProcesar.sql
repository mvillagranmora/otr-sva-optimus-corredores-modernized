/********************************************************** 
Proposito	: Script para obtener mensajes de precio mercado sin procesar
autor		: PROJO
fecha		: 03/03/2020
***********************************************************/
SELECT cm."IdMensaje1",cm."IdMensaje2",ins."IdInstrumento",ins."IdTipoInstrumento"
,insop."codTicker",insop."timeTicker",insop."codIsin",insop."action",insop."instruments",insop."imbalance",insop."tradeVolume",insop."avgPrice",insop."tradeAmount",insop."trend",insop."cfiCode"
,insop."maturityDate",insop."issuer",insop."eventType",insop."settlement",insop."exercise",insop."openContract",insop."StrikePrice",insop."minPrice",insop."highPrice",insop."roundLot", insop."securitySubType"
FROM "BSCL.Orden.BD::ORD.ControlMercado" cm
LEFT JOIN "BSCL.Orden.BD::MOP.INSTRUMENTOS_OP" insop on cm."IdMensaje2" = insop."instruments"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."Nemotecnico" = insop."instruments" AND ins."OrigenInstrumento"='O' 
WHERE cm."TipoMensaje" IN('INGRESO_INSTRUMENTOS_OP') AND cm."Procesado" = 'N' AND insop."codTicker" = 'INSTRUMENT' ORDER BY cm."IdMensaje2" DESC




