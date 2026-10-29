UPDATE "BSCL.Plataforma.BD::DOC.Anexo"
SET "IdEstado"        = :IdEstado
  , "FechaActivacion" = :FechaActivacion
WHERE "IdAnexo" = :IdAnexo;