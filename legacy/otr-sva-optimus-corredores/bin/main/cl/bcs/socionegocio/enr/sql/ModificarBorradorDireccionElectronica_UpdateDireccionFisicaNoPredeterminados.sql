UPDATE "BSCL.SocioNegocio.BD::ENR.DireccionFisica"
SET "Predeterminado" = 'N'
WHERE "IdEnrolamiento" = :IdEnrolamiento AND "IdDireccionFisica" != :IdDireccionFisica