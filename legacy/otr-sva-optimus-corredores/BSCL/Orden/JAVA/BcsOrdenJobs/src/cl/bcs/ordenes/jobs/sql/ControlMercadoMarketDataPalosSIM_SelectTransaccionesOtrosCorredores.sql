SELECT cm."TipoMensaje" AS "_TipoMensaje",cm."IdMensaje1" AS "_IdMensaje1",cm."IdMensaje2" AS "_IdMensaje2",
tx."buyExecutingFirm",tx."sellExecutingFirm",tx."folio",tx."transactionDay"
FROM "BSCL.Orden.BD::ORD.ControlMercado" cm 
LEFT JOIN "BSCL.Orden.BD::MRV.PALOS_SERIALIZADOS" tx on cm."IdMensaje1" = tx."transactionTime" and cm."IdMensaje2" = tx."folio" || tx."action"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" buyer on buyer."CodigoCorredoraFix" = tx."buyExecutingFirm"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" seller on seller."CodigoCorredoraFix" = tx."sellExecutingFirm"
WHERE cm."Procesado" = 'N' and cm."TipoMensaje" = 'MARKET_DATA_PALO_SERIALIZADO'
and buyer."CodigoCorredoraFix" is null and seller."CodigoCorredoraFix" is null;