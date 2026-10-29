UPDATE "BSCL.Plataforma.BD::ACC.Rol" 
SET "Nombre" = :Nombre, "IdEstado" = :IdEstado
WHERE "IdRol" = :IdRol AND "IdEmpresa" = :IdEmpresa
