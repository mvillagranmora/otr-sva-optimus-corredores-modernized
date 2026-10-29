UPDATE "BSCL.SocioNegocio.BD::PER.TarjetaBancaria"
SET "IdTipoTarjetaBancaria" = :p_ValorNuevo, "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdTarjetaBancaria" = :p_IdTarjetaBancaria
