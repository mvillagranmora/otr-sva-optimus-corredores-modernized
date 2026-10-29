SELECT "IdCliente" AS "IdCliente",
       "IdDeclaracionOrigenFondos" AS "IdDeclaracionOrigenFondos"
FROM "BSCL.SocioNegocio.BD::CLI.DeclaracionOrigenFondos"
WHERE "IdDeclaracionOrigenFondos" IN (:IdDeclaracionOrigenFondos)
