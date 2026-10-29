SELECT "IdPersona" AS "IdPersona",
       "IdTarjetaBancaria" AS "IdTarjetaBancaria"
FROM "BSCL.SocioNegocio.BD::PER.TarjetaBancaria"
WHERE "IdTarjetaBancaria" IN (:IdTarjetaBancaria)
