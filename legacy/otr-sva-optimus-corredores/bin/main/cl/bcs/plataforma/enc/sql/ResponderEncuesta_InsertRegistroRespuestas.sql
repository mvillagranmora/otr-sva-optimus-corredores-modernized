INSERT INTO "BSCL.SocioNegocio.BD::CLI.RegistroRespuesta"
("IdEmpresa", "IdRegistroRespuesta", "IdRegistroPregunta", "FechaIngresoRegistro", "TextoRespuesta", "Puntaje", "Seleccionado")
VALUES(:IdEmpresa, :IdRegistroRespuesta, :IdRegistroPregunta, CURRENT_UTCTIMESTAMP, :TextoRespuesta, :Puntaje, :Seleccionado);
