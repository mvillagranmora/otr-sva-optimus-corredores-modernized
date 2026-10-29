SELECT ED."IdEventoDetalle" AS "IdEventoDetalle"
     , ED."IdEvento"        AS "IdEvento"
     , ED."NombreCampo"     AS "NombreCampo"
     , ED."Valor"           AS "Valor"
FROM "BSCL.Plataforma.BD::AUT.EventoDetalle" ED
WHERE ED."IdEvento" IN (:IdsEventos);