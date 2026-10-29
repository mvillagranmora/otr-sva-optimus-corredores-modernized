SELECT EVT."IdEvento", EVT."IdEstado"
FROM "BSCL.Plataforma.BD::AUT.Evento" AS EVT
WHERE EVT."IdTipoEvento" = :IdTipoEvento AND EVT."Fecha" = :FechaSistema ORDER BY EVT."IdEvento" DESC LIMIT 1;