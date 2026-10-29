UPDATE "BSCL.Plataforma.BD::EVT.Evento" SET "Procesado" = 'N' ,"FechaCreacion" = CURRENT_TIMESTAMP  WHERE "IdEvento" = :IdEvento;
