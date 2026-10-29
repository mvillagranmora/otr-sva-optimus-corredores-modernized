INSERT INTO "BSCL.Plataforma.BD::ENC.DefinicionEncuesta"
("IdEmpresa", "IdDefinicionEncuesta", "Borrado","FechaIngresoRegistro", "IdUsuarioCreacion", "Nombre", "IdEstado")
VALUES(:IdEmpresa, :IdDefinicionEncuesta, :Borrado,CURRENT_UTCTIMESTAMP, :IdUsuarioCreacion, :Nombre, :IdEstado);
