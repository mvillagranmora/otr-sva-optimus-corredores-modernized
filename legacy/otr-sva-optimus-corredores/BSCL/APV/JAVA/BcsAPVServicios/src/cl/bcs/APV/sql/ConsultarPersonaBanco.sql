SELECT "IdPersona" AS "IdPersonaBanco", "IdBanco" AS "IdBanco"
FROM "BSCL.Plataforma.BD::BAN.Banco"
WHERE "IdBanco" IN (:IdsBanco);