UPDATE "BSCL.SocioNegocio.BD::PER.DireccionElectronica" SET "Borrado" = :p_Borrado, "FechaModificacion" = CURRENT_UTCTIMESTAMP WHERE "IdDireccionElectronica" = :p_IdDireccionElectronica
