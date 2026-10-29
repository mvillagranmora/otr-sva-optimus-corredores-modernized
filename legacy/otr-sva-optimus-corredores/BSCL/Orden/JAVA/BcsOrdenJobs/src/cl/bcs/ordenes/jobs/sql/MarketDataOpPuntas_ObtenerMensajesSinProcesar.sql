/********************************************************** 
Proposito	: Script para obtener mensajes de precio mercado sin procesar
autor		: projo
fecha		: 13/03/2020
***********************************************************/
SELECT distinct cm."IdMensaje1",cm."IdMensaje2",ins."IdInstrumento",ins."IdTipoInstrumento",
	 punta."codTicker",punta."timeStamp",punta."action",punta."symbol",punta."amount",punta."amountSale"
	,punta."price",punta."priceSale",punta."settlement",punta."buyExecutingFirm",punta."buyEnteringFirm"
	,punta."buyEnteringTrader",punta."sellExecutingFirm",punta."sellEnteringFirm",punta."sellEnteringTrader"
	,punta."entryPositionNo",punta."bookingRefID"
FROM "BSCL.Orden.BD::ORD.ControlMercado" cm
LEFT JOIN "BSCL.Orden.BD::MOP.PUNTAS_OP" punta on cm."IdMensaje2" = punta."symbol" and cm."IdMensaje1" = punta."timeStamp"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."Nemotecnico" = punta."symbol" 
WHERE "TipoMensaje" = 'PUNTA_OP' AND "Procesado" = 'N' ORDER BY punta."timeStamp";
