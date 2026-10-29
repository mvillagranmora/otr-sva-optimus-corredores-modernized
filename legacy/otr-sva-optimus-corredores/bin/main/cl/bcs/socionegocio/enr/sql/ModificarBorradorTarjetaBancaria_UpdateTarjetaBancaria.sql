UPDATE "BSCL.SocioNegocio.BD::ENR.TarjetaBancaria"
SET "IdBanco" = :IdBanco
,"IdTipoTarjetaBancaria" = :IdTipoTarjetaBancaria
,"NumeroTarjetaBancaria" = :NumeroTarjetaBancaria
,"FechaVencimiento" = :FechaVencimiento
,"CodigoVerificacion" = :CodigoVerificacion
WHERE "IdTarjetaBancaria" = :IdTarjetaBancaria