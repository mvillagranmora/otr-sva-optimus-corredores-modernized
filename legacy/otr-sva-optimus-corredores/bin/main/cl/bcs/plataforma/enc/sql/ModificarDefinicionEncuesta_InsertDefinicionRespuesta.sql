INSERT INTO "BSCL.Plataforma.BD::ENC.DefinicionRespuesta"
("IdEmpresa", "IdDefinicionRespuesta", "IdDefinicionPregunta", "IdUsuarioCreacion", "Borrado","FechaIngresoRegistro", "TextoRespuesta", "Puntaje")
VALUES(:IdEmpresa, "BSCL.Plataforma.BD.Secuencias::IdDefinicionRespuesta".NEXTVAL,:IdDefinicionPregunta, :IdUsuarioCreacion, :Borrado,CURRENT_UTCTIMESTAMP, :TextoRespuesta, :Puntaje);
