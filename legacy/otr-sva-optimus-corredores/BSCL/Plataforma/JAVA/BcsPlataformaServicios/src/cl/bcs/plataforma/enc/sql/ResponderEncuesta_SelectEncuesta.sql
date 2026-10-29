SELECT "IdEmpresa","IdDefinicionEncuesta","FechaIngresoRegistro","IdUsuarioCreacion","Nombre","IdEstado"
FROM "BSCL.Plataforma.BD::ENC.DefinicionEncuesta"
WHERE "IdDefinicionEncuesta" = :IdDefinicionEncuesta
