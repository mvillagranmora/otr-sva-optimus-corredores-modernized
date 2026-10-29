INSERT INTO "BSCL.SocioNegocio.BD::CLI.RegistroPregunta"
("IdEmpresa", "IdRegistroEncuesta", "IdRegistroPregunta", "FechaIngresoRegistro", "TextoPregunta")
VALUES(:IdEmpresa, :IdRegistroEncuesta, :IdRegistroPregunta, CURRENT_UTCTIMESTAMP, :TextoPregunta);
