SELECT DIRECCION_FISICA."IdDireccionFisica"
FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DIRECCION_FISICA
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA ON DIRECCION_FISICA."IdDireccionFisica" = PERSONA."IdDireccionFisicaPrincipal"
WHERE DIRECCION_FISICA."IdPersona" IN (:IdPersona) AND DIRECCION_FISICA."Borrado" = 'N' AND PERSONA."IdPersona" IS NULL;