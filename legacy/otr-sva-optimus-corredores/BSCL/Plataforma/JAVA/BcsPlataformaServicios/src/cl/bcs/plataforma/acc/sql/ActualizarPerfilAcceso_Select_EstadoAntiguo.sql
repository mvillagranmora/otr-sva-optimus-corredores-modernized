SELECT COALESCE("IdEstado",'') AS "IdEstado" 
FROM "BSCL"."BSCL.Plataforma.BD::ACC.Perfil"
WHERE "IdPerfil" = :IdPerfil 
  AND "IdEmpresa" = :IdEmpresa