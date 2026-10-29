SELECT DISTINCT U."CodigoUsuario" AS "SubRegistro", 
                P."Nombre" AS "CampoRegistro", 
                :v_EstadoAntiguo AS "ValorAntiguo", 
                P."IdEstado" AS "ValorNuevo"
FROM "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" AS U
INNER JOIN "BSCL"."BSCL.Plataforma.BD::ACC.RolUsuario" AS RU 
    ON U."IdUsuario" = RU."IdUsuario"
INNER JOIN "BSCL"."BSCL.Plataforma.BD::ACC.Rol" AS R 
    ON RU."IdRol" = R."IdRol"
INNER JOIN "BSCL"."BSCL.Plataforma.BD::ACC.RolPerfil" AS RP 
    ON RP."IdRol" = R."IdRol"
INNER JOIN "BSCL"."BSCL.Plataforma.BD::ACC.Perfil" AS P 
    ON P."IdPerfil" = RP."IdPerfil"
WHERE RP."IdPerfil" = :IdPerfil