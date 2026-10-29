SELECT EVT."IdEvento"
FROM "BSCL.Plataforma.BD::AUT.Evento" AS EVT
WHERE EVT."IdTipoEvento" = :IdTipoEvento AND EVT."Fecha" = :FechaSistema AND EVT."IdEstado" = 'VIG';