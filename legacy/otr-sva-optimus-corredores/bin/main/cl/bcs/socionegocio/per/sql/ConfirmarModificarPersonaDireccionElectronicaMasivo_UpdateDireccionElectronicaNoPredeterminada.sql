UPDATE "BSCL.SocioNegocio.BD::PER.DireccionElectronica"
SET "Predeterminado" = 'N'
WHERE "IdDireccionElectronica" <> (:IdDireccionElectronica) AND "IdPersona" = (:IdPersona)