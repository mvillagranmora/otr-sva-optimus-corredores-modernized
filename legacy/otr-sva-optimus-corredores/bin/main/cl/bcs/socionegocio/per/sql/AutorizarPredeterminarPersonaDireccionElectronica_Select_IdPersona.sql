SELECT 
    "IdPersona" AS "IdPersona",
    "IdDireccionElectronica" AS "IdDireccionElectronica"
FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica"
WHERE "IdDireccionElectronica" = :IdDireccionElectronica
