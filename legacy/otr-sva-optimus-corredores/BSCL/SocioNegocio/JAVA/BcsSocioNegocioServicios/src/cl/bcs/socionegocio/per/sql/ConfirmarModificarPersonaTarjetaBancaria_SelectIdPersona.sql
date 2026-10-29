SELECT "IdTarjetaBancaria" AS "IdTarjetaBancaria", "IdPersona" AS "IdPersona"
FROM "BSCL.SocioNegocio.BD::PER.TarjetaBancaria"
WHERE "IdTarjetaBancaria" IN (:IdTarjetaBancaria);
