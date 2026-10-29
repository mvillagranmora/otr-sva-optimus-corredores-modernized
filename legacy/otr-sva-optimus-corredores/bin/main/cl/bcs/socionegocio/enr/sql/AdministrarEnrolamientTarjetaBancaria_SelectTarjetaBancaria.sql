SELECT "IdTarjetaBancaria"
FROM "BSCL.SocioNegocio.BD::PER.TarjetaBancaria"
WHERE "IdPersona" IN (:IdPersona) AND "Borrado" = 'N'