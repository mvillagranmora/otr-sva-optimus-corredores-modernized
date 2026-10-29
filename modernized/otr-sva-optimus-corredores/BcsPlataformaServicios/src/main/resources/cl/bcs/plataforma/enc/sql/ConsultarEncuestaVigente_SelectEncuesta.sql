SELECT TOP 1 "IdEmpresa","IdDefinicionEncuesta","FechaIngresoRegistro","IdUsuarioCreacion","Nombre","IdEstado"
FROM "BSCL.Plataforma.BD::ENC.DefinicionEncuesta"
WHERE "IdEstado" = 'ACT' AND "Borrado" = 'N'
