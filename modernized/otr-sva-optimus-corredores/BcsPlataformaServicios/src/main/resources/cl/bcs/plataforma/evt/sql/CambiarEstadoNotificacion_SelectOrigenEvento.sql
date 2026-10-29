SELECT E."IdOrigenEvento", N."IdNotificacion", N."Enviar"
FROM "BSCL.Plataforma.BD::EVT.Notificacion" N
INNER JOIN "BSCL.Plataforma.BD::EVT.NotificacionDetalle" ND ON ND."IdNotificacion" = N."IdNotificacion"
INNER JOIN "BSCL.Plataforma.BD::EVT.Evento" E ON E."IdEvento" = ND."IdEvento"
WHERE E."IdOrigenEvento" IN (:IdsOrigenEvento);