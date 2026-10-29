SELECT "BSCL.Orden.BD.Secuencias::MarketData_IdInterno".NEXTVAL as "IdInterno",cm."TipoMensaje" AS "_TipoMensaje",cm."IdMensaje1" AS "_IdMensaje1",cm."IdMensaje2" AS "_IdMensaje2",
COALESCE(tx."CODTICKER",'') AS "CODTICKER",tx."TIMETICKER",tx."ACTION",tx."TRADETIME",tx."TRADEID",tx."SYMBOL",tx."CONDISIN",tx."BUYEXECUTINGFIRM",
tx."SELLEXECUTINGFIRM",tx."BUYENTERINGFIRM",tx."SELLENTERINGFIRM",tx."TRADECONDITION",tx."CONDITION",tx."ISSUER",tx."FAMILY",tx."ENTERINGDATE",
tx."EXPIRETIME",tx."LAMINA",tx."AMOUNT",tx."TERM",tx."AUCTIONID",tx."RATE",tx."SIGNRATE",tx."RESETTYPE",tx."RESCUEVALUE",tx."TRADERBUY",
tx."TRADERSELL",tx."DCVTITLE",tx."SETTLDATE",tx."SYMBOLSVS",tx."LOCALAMOUNT",tx."BUYERFUND",tx."SELLERFUND"
from "BSCL.Orden.BD::ORD.ControlMercado" cm 
LEFT JOIN "BSCL.Orden.BD::MRF.PALOS_IF" tx on cm."IdMensaje1" = tx."TIMETICKER" and cm."IdMensaje2" = tx."TRADEID"
where cm."Procesado" = 'N' AND cm."TipoMensaje" = 'MARKET_DATA_PALO_IF';