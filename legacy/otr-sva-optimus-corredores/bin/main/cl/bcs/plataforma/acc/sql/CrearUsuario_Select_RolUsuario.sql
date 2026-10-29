SELECT USU.*
FROM "BSCL.Plataforma.BD::ACC.RolUsuario" ROL
INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" as USU ON USU."IdUsuario" = ROL."IdUsuario"
WHERE "IdEmpresa" = :IdEmpresa AND "IdRol" = :IdRol AND USU."IdUsuario" =:IdUsuario;