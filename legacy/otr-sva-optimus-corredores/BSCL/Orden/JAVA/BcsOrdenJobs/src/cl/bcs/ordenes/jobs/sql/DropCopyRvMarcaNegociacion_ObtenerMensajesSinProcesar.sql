/********************************************************** 
Proposito	: Script para obtener mensajes de marca de negociacion de drop copy
autor		: RU
fecha		: 01/02/2017
***********************************************************/
SELECT cm."IdMensaje1",cm."IdMensaje2",dcrv."ORDERID",dcrv."EXECID",dcrv."SIDE",dcrv."ORDSTATUS",dcrv."EXECTYPE",dcrv."TRADEDATE",dcrv."TRADEID",dcrv."CLIENTID",dcrv."TRANSACTTIME",dcrv."ENTERINGFIRM",dcrv."EXECUTINGFIRM",dcrv."ENTERINGTRADER"
FROM "BSCL.Orden.BD::ORD.ControlMercado" cm
LEFT JOIN "BSCL.Orden.BD::DPC.TBEXECUTIONREPORT" dcrv on cm."IdMensaje1" = dcrv."ORDERID" and cm."IdMensaje2" = dcrv."EXECID"
WHERE "TipoMensaje" = 'DROPCOPYRV_MARCA_NEGOCIACION' AND "Procesado" = 'N' ORDER BY dcrv."EXECID";