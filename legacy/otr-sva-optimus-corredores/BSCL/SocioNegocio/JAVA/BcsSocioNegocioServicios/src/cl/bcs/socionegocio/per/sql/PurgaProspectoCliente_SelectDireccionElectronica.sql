SELECT "IdPersona", "IdDireccionElectronica", "Direccion" 
FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica" 
WHERE "IdPersona" IN (:IdsPersonas);