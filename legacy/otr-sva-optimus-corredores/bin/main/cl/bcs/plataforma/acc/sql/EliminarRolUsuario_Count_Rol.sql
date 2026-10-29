SELECT COUNT("IdRol") AS "Contador" 
FROM "BSCL.Plataforma.BD::ACC.Rol" 
WHERE "IdEmpresa" = :IdEmpresa 
  AND "IdRol" = :IdRol 
  AND "IdEstado" = 'ACT'
