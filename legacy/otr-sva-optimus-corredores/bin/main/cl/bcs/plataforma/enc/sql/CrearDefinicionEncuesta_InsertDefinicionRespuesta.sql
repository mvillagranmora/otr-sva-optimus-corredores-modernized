INSERT INTO "BSCL.Plataforma.BD::ENC.DefinicionRespuesta"
("IdEmpresa", "IdDefinicionRespuesta", "IdDefinicionPregunta", "Borrado","IdUsuarioCreacion", "FechaIngresoRegistro", "TextoRespuesta", "Puntaje")
VALUES(:IdEmpresa, :IdDefinicionRespuesta, :IdDefinicionPregunta, :Borrado,:IdUsuarioCreacion, CURRENT_UTCTIMESTAMP, :TextoRespuesta, :Puntaje);
