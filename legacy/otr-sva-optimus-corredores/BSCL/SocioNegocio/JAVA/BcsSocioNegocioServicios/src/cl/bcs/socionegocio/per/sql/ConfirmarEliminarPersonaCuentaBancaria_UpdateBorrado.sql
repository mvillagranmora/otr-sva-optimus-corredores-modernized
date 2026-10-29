UPDATE "BSCL.SocioNegocio.BD::PER.CuentaBancaria" SET "Borrado" = :p_Borrado, "FechaModificacion" = CURRENT_UTCTIMESTAMP WHERE "IdCuentaBancaria" = :p_IdCuentaBancaria
