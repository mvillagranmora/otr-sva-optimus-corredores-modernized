UPDATE "BSCL.SocioNegocio.BD::ENR.Telefono"
SET "Predeterminado" = 'N'
WHERE "IdEnrolamiento" = :IdEnrolamiento AND "IdTelefono" != :IdTelefono