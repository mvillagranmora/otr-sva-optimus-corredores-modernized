SELECT "IdPersona", "IdTarjetaBancaria", "NumeroTarjetaBancaria" 
FROM "BSCL.SocioNegocio.BD::PER.TarjetaBancaria"
WHERE "IdPersona" IN (:IdsPersonas);