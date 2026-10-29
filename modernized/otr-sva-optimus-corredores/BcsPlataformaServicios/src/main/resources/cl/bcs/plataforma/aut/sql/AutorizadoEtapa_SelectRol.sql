SELECT
"IdRol"
,"IdEmpresa"
,"Nombre"
,"IdEstado"
FROM "BSCL.Plataforma.BD::ACC.Rol"
WHERE "IdEmpresa" = :IdEmpresa
AND "IdRol" IN (:IdsRoles);