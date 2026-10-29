SELECT "IdDireccionFisica" AS "IdDireccionFisica", "IdPersona" AS "IdPersona"
FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica"
WHERE "IdDireccionFisica" IN (:IdDireccionFisica);
