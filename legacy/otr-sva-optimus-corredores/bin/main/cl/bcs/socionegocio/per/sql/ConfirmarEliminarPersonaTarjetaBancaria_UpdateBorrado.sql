UPDATE "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" SET "Borrado" = :p_Borrado, "FechaModificacion" = CURRENT_UTCTIMESTAMP WHERE "IdTarjetaBancaria" = :p_IdTarjetaBancaria
