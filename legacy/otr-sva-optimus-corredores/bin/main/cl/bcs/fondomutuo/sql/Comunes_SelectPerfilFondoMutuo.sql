SELECT TOP 1 "Key" AS "Perfil"
FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" AS "Configuracion"
INNER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfilesUsuario" AS "Perfiles" ON "Perfiles"."IdPerfil" = "Configuracion"."Value"
WHERE "Key" IN ('PERFIL_COMERCIAL', 'PERFIL_BACKOFFICE') AND "Perfiles"."IdUsuario" = :IdUsuario;