UPDATE "BSCL.SocioNegocio.BD::PER.TarjetaBancaria"
SET "CodigoVerificacion" = :p_ValorNuevo, "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdTarjetaBancaria" = :p_IdTarjetaBancaria
