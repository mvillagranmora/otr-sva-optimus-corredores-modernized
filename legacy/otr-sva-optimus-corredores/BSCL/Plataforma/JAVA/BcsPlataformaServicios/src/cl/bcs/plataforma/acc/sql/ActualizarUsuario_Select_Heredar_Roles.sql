SELECT COUNT(1) AS "Contador" FROM "BSCL.Plataforma.BD::ACC.Usuario"
WHERE "IdUsuario" = :HeredarRolesUsuario AND "IdEmpresa" = :IdEmpresa;