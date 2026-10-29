SELECT M."CodigoModulo" , M."IdEstado" AS "Estado"
FROM "BSCL.Plataforma.BD::PAU.Modulo" AS M
WHERE M."CodigoModulo" = 'SIS' AND M."IdEmpresa"= :p_IdEmpresa;