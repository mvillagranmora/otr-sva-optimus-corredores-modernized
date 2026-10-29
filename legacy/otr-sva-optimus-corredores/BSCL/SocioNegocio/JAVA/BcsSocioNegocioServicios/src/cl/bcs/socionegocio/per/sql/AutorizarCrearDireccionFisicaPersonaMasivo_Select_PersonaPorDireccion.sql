SELECT 
    "IdPersona" AS "IdPersona",
    "IdDireccionFisica" AS "IdDireccionFisica"
FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica"
WHERE "IdDireccionFisica" IN (:IdDireccionFisica)
