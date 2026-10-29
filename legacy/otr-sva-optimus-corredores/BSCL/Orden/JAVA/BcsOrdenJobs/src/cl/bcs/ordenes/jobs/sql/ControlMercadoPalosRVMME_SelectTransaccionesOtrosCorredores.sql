SELECT cm."TipoMensaje" AS "_TipoMensaje",cm."IdMensaje1" AS "_IdMensaje1",cm."IdMensaje2" AS "_IdMensaje2",
CASE WHEN tr."SIDE"=1 THEN RIGHT(tr."EXECUTINGFIRM",3) WHEN tr."SIDE" IN (2,5) THEN RIGHT(tr."CONTRAFIRM",3) END AS "buyerBroker",
CASE WHEN tr."SIDE"=1 THEN RIGHT(tr."CONTRAFIRM",3) WHEN tr."SIDE" IN (2,5) THEN RIGHT(tr."EXECUTINGFIRM",3) END AS "sellerBroker",
tr."SIDETRADEID" as "tradeID",TO_NVARCHAR(tr."TIMESTAMP")
FROM "BSCL.Orden.BD::ORD.ControlMercado" cm 
LEFT JOIN "BSCL.Orden.BD::MME.PALOS_RV_DROP" tr on cm."IdMensaje1" = TO_NVARCHAR(tr."TIMESTAMP") and cm."IdMensaje2" = tr."SIDETRADEID" || tr."TRADEREPORTTYPE"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" buyer on buyer."CodigoCorredoraFix" = CASE WHEN tr."SIDE"=1 THEN RIGHT(tr."EXECUTINGFIRM",3) WHEN tr."SIDE" IN (2,5) THEN RIGHT(tr."CONTRAFIRM",3) END
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" seller on seller."CodigoCorredoraFix" = CASE WHEN tr."SIDE"=1 THEN RIGHT(tr."CONTRAFIRM",3) WHEN tr."SIDE" IN (2,5) THEN RIGHT(tr."EXECUTINGFIRM",3) END
WHERE cm."Procesado" = 'N' and cm."TipoMensaje" = 'MARKET_DATA_PALO_RV_MME'
and buyer."CodigoCorredoraFix" is null and seller."CodigoCorredoraFix" is null;