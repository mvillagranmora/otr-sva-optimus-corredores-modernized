UPDATE "BSCL.Plataforma.BD::AUT.Evento" Evento
SET
    Evento."IdEstado" = :IdEstadoPosteriorAutorizacion
WHERE Evento."IdEvento" = :IdEvento