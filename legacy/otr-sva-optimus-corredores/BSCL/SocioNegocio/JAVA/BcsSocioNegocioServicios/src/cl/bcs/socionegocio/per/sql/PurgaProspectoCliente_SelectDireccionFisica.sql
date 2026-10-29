SELECT "IdPersona", "IdDireccionFisica", "IdCiudad", "CodigoPostal", "Calle", "Numero", "Departamento" 
FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" 
WHERE "IdPersona" IN (:IdsPersonas);