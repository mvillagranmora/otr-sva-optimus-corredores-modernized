UPDATE "BSCL"."BSCL.Plataforma.BD::ACC.Perfil" 
SET "Nombre" = :Nombre, 
    "IdEstado" = :IdEstado 
WHERE "IdPerfil" = :IdPerfil 
  AND "IdEmpresa" = :IdEmpresa