UPDATE "BSCL.SocioNegocio.BD::PER.Telefono"
SET "Predeterminado" = 'S',
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdTelefono" = :IdTelefono
