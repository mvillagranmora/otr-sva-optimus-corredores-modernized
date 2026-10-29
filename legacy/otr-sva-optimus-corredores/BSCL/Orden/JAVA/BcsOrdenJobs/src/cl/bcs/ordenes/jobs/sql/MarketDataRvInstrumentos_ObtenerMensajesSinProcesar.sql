/********************************************************** 
Proposito	: Script para obtener mensajes de precio mercado sin procesar
autor		: RU
fecha		: 01/02/2017
***********************************************************/
SELECT ins."IdInstrumento",ins."IdTipoInstrumento",insrv."codTicker",insrv."timeStamp",insrv."action",insrv."instruments",insrv."codIsin",insrv."market",
insrv."minPrice",insrv."openPrice",insrv."highPrice",insrv."imbalance",insrv."tradeVolume",insrv."avgPrice",insrv."tradeAmount",insrv."closingPrice",insrv."trend",insrv."dcv",
insrv."instrumentType",insrv."timeStampLastTrade",insrv."lastPrice",insrv."quantityLastTrade"
FROM "BSCL.Orden.BD::MRV.INSTRUMENTOS_RV" insrv 
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."Nemotecnico" = insrv."instruments" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tins on tins."IdTipoInstrumento" = ins."IdTipoInstrumento"
WHERE COALESCE(tins."IdClasificacionTipoInstrumento", '') = CASE WHEN COALESCE(tins."IdClasificacionTipoInstrumento", '') <> '' THEN 'RV' ELSE '' END
ORDER BY insrv."instruments";
