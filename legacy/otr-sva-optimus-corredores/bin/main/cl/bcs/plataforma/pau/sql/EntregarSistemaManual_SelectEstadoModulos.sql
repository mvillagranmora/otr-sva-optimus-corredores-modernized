SELECT "CodigoModulo" as "IdModulo",PLAMOD."Nombre" AS "NombreModulo",PAUMOD."IdEstado"
FROM "BSCL.Plataforma.BD::PAU.Modulo" PAUMOD
LEFT JOIN "BSCL.Plataforma.BD::PLA.Modulo" PLAMOD ON PAUMOD."CodigoModulo" = PLAMOD."IdModulo"
WHERE "IdEmpresa" = :IdEmpresa