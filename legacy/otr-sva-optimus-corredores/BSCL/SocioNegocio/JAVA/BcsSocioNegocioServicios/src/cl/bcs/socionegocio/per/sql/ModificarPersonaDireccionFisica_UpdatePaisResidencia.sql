UPDATE PER
SET PER."IdPaisResidencia" = CIU."IdPais"
FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
INNER JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" DIR ON (PER."IdPersona" = DIR."IdPersona")
INNER JOIN "BSCL.Plataforma.BD::REG.Ciudad" CIU ON (DIR."IdCiudad" = CIU."IdCiudad")
WHERE DIR."IdDireccionFisica" = :p_IdDireccionFisica AND DIR."Predeterminado" = 'S'
