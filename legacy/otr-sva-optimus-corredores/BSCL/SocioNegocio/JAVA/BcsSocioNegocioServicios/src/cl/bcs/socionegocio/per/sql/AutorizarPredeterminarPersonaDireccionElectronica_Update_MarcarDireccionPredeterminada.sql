UPDATE "BSCL.SocioNegocio.BD::PER.DireccionElectronica"
SET 
    "Predeterminado" = 'S',
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDireccionElectronica" = :IdDireccionElectronica
