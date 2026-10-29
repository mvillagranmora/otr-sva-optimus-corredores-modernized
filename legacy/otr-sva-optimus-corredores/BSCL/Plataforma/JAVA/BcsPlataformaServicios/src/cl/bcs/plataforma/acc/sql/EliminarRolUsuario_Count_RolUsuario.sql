SELECT COUNT("IdRolUsuario") AS "Contador" 
FROM "BSCL.Plataforma.BD::ACC.RolUsuario"
WHERE "IdRol" = :IdRol 
  AND "IdUsuario" = :IdUsuario
