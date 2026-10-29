UPDATE "BSCL.Plataforma.BD::ENC.DefinicionEncuesta"
SET "IdEstado" = 'INA'
WHERE "IdDefinicionEncuesta" != :IdDefinicionEncuesta;
