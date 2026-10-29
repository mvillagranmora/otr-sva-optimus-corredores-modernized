SELECT 
    COALESCE("Direccion", '') AS "Direccion"
FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica"
WHERE "IdDireccionElectronica" = :IdDireccionElectronica
