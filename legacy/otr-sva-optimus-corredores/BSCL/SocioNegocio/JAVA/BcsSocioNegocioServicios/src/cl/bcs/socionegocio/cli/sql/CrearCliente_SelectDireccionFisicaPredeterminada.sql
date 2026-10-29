SELECT "IdDireccionFisica" FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica"
WHERE "IdPersona" IN (:IdPersona) AND "Predeterminado" = 'S';