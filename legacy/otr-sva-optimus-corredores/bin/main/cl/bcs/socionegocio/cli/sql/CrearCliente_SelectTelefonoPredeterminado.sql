SELECT "IdTelefono" AS "IdTelefonoContacto" FROM "BSCL.SocioNegocio.BD::PER.Telefono"
WHERE "IdPersona" IN (:IdPersona) AND "Predeterminado" = 'S';