UPDATE "BSCL.SocioNegocio.BD::ENR.DocumentoIdentidad"
SET "Predeterminado" = 'N'
WHERE "IdEnrolamiento" = :IdEnrolamiento AND "IdDocumentoIdentidad" != :IdDocumentoIdentidad