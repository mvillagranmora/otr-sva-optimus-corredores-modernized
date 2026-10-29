UPDATE "BSCL.Plataforma.BD::DOC.Anexo"
SET    "IdEstado" = :p_estado
WHERE  "IdAnexo"=:p_IdUltimoAnexo AND "IdDocumento"=:p_numerodocumento;