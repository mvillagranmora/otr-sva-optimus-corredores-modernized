UPDATE "BSCL.SocioNegocio.BD::PER.DireccionFisica"
SET 
    "Predeterminado" = 'S',
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDireccionFisica" = :IdDireccionFisica
