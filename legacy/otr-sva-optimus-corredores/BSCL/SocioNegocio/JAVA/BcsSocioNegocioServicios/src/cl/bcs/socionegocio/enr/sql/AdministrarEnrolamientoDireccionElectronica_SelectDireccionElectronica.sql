SELECT DIRECCION_ELECTRONICA."IdDireccionElectronica"
FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DIRECCION_ELECTRONICA
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA ON DIRECCION_ELECTRONICA."IdDireccionElectronica" = PERSONA."IdDireccionElectronicaPrincipal"
WHERE DIRECCION_ELECTRONICA."IdPersona" IN (:IdPersona) AND DIRECCION_ELECTRONICA."Borrado" = 'N' AND PERSONA."IdPersona" IS NULL;