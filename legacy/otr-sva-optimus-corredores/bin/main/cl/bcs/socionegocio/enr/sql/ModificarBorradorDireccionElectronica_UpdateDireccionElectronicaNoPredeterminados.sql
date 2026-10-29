UPDATE "BSCL.SocioNegocio.BD::ENR.DireccionElectronica"
SET "Predeterminado" = 'N'
WHERE "IdEnrolamiento" = :IdEnrolamiento AND "IdDireccionElectronica" != :IdDireccionElectronica