SELECT "IdEntidad" AS "IdEntidad",
       "TipoEntidad" AS "TipoEntidad"
FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto"
WHERE "IdUsuario" = :IdUsuario
