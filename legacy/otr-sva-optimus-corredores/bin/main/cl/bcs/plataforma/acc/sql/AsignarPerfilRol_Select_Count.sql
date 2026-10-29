SELECT COUNT("IdRolPerfil") AS "Contador" 
FROM "BSCL.Plataforma.BD::ACC.RolPerfil" 
WHERE "IdRol" = :IdRol AND "IdPerfil" = :IdPerfil
