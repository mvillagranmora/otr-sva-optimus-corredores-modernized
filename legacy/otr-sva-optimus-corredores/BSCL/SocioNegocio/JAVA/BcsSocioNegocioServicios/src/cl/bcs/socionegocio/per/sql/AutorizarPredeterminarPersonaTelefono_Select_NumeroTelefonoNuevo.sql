SELECT COALESCE("Numero", '') AS "Numero"
FROM "BSCL.SocioNegocio.BD::PER.Telefono"
WHERE "IdTelefono" = :IdTelefono
