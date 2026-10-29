SELECT "IdPersona", "IdTelefono", "Numero" 
FROM "BSCL.SocioNegocio.BD::PER.Telefono"
WHERE "IdPersona" IN (:IdsPersonas);