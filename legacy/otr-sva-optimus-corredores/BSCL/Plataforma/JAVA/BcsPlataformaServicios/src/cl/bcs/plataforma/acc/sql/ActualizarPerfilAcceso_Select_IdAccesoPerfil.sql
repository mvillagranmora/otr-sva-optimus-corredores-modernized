SELECT "IdAccesoPerfil"
FROM "BSCL"."BSCL.Plataforma.BD::ACC.PerfilAcceso"
WHERE "IdAcceso" = :IdAcceso 
  AND "IdPerfil" = :IdPerfil 
  AND "IdEmpresa" = :IdEmpresa