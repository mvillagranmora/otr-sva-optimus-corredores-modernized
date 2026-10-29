SELECT TELEFONO."IdTelefono"
FROM "BSCL.SocioNegocio.BD::PER.Telefono" AS TELEFONO
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA ON TELEFONO."IdTelefono" = PERSONA."IdTelefonoPrincipal"
WHERE TELEFONO."IdPersona" IN (:IdPersona) AND TELEFONO."Borrado" = 'N' AND PERSONA."IdPersona" IS NULL;