UPDATE "BSCL.SocioNegocio.BD::PER.DireccionFisica"
SET "IdCiudad" = :p_ValorNuevo, "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDireccionFisica" = :p_IdDireccionFisica
