UPDATE "BSCL.Plataforma.BD::EVT.Mensaje"
SET "Habilitado"='N'
WHERE "IdTipoMensaje" = :IdTipoMensaje AND "Habilitado" = 'S';