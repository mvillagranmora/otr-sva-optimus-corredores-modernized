UPDATE "BSCL.SocioNegocio.BD::PER.DireccionFisica"
SET "Predeterminado" = 'N'
WHERE "IdDireccionFisica" <> (:IdDireccionFisica) AND "IdPersona" = (:IdPersona)