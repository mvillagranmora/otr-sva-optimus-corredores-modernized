SELECT P."IdEmpresa" AS "IdEmpresa",P."CodigoModulo" AS "IdModulo",P."IdEstado" AS "IdEstado", X."Nombre" AS "NombreModulo"
FROM "BSCL.Plataforma.BD::PAU.Modulo" AS  P
LEFT JOIN "BSCL.Plataforma.BD::PLA.Modulo" AS X ON P."CodigoModulo" = X."IdModulo";