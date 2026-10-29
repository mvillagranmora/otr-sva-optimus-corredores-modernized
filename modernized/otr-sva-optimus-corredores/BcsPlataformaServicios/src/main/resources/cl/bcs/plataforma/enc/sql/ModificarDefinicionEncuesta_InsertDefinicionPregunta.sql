INSERT INTO "BSCL.Plataforma.BD::ENC.DefinicionPregunta"
("IdEmpresa", "IdDefinicionEncuesta", "IdDefinicionPregunta", "Borrado","IdUsuarioCreacion", "FechaIngresoRegistro", "TextoPregunta")
VALUES(:IdEmpresa, :IdDefinicionEncuesta, :IdDefinicionPregunta, :Borrado,:IdUsuarioCreacion, CURRENT_UTCTIMESTAMP, :TextoPregunta);
