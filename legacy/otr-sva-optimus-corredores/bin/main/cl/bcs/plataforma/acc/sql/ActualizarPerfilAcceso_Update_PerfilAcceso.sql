UPDATE "BSCL"."BSCL.Plataforma.BD::ACC.PerfilAcceso" 
SET "IdEstado" = :IdEstado, 
    "AsignoUsuario" = :AsignoUsuario, 
    "FechaAsignacion" = CURRENT_UTCTIMESTAMP
WHERE "IdAcceso" = :IdAcceso 
  AND "IdPerfil" = :IdPerfil 
  AND "IdEmpresa" = :IdEmpresa