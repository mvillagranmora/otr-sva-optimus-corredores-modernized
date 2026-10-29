SELECT "EstadoRol","NombreRol"
FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesUsuario" 
WHERE "EstadoRol" = 'ACTIVO' AND "IdUsuario" = :IdUsuario AND "IdEmpresa" = :IdEmpresa;
