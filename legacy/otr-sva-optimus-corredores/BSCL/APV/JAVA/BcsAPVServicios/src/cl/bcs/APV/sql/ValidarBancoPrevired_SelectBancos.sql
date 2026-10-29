SELECT 
"IdBanco"
,"NombreBanco"
,"CodigoBanco"
FROM "BSCL.Plataforma.BD::BAN.Banco"
WHERE "IdBanco" IN (:IdBanco);