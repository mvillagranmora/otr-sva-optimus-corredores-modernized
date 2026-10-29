SELECT COALESCE("IdEstado", '') AS "IdEstado" 
FROM "BSCL.Plataforma.BD::ACC.Rol"
WHERE "IdRol" = :IdRol AND "IdEmpresa" = :IdEmpresa
