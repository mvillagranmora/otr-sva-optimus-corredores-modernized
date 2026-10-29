SELECT "IdTelefono" AS "IdTelefonoContacto" FROM "BSCL.SocioNegocio.BD::PER.Telefono"
WHERE "IdPersona" = (:IdPersona) AND "Predeterminado" = 'S'
