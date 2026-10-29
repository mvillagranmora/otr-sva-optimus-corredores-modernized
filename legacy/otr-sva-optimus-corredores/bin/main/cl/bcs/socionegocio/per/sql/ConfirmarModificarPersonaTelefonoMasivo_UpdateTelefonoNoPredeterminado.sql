UPDATE "BSCL.SocioNegocio.BD::PER.Telefono"
SET "Predeterminado" = 'N'
WHERE "IdTelefono" <> (:IdTelefono) AND "IdPersona" = (:IdPersona)