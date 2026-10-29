SELECT cm."TipoMensaje" AS "_TipoMensaje",cm."IdMensaje1" AS "_IdMensaje1",cm."IdMensaje2" AS "_IdMensaje2",tx."buyerBroker",tx."sellerBroker",tx."tradeID",tx."timeStamp"
FROM "BSCL.Orden.BD::ORD.ControlMercado" cm 
LEFT JOIN "BSCL.Orden.BD::MRV.PALOS_RV" tx on cm."IdMensaje1" = tx."timeStamp" and cm."IdMensaje2" = tx."tradeID" || tx."action"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" buyer on buyer."CodigoCorredoraFix" = tx."buyerBroker"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" seller on seller."CodigoCorredoraFix" = tx."sellerBroker"
WHERE cm."Procesado" = 'N' and cm."TipoMensaje" = 'MARKET_DATA_PALO_RV'
and buyer."CodigoCorredoraFix" is null and seller."CodigoCorredoraFix" is null;
