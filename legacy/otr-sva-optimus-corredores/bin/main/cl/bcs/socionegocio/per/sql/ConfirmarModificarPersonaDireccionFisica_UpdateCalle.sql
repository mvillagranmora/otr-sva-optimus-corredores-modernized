UPDATE "BSCL.SocioNegocio.BD::PER.DireccionFisica"
SET "Calle" = :p_ValorNuevo, "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDireccionFisica" = :p_IdDireccionFisica
