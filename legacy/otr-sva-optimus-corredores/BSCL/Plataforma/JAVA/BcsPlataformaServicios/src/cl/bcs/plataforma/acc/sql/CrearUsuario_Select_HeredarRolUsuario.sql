SELECT COUNT(1) AS "Count" 
FROM "BSCL.Plataforma.BD::ACC.Usuario"
WHERE "IdUsuario" = :IdUsuario AND "IdEmpresa" = :IdEmpresa;