SELECT "IdSucursal", "IdUsuario"
FROM "BSCL.Plataforma.BD::ACC.Usuario" as USU 
INNER JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" as EMP ON (EMP."IdEmpleado" = USU."IdEntidad") 
WHERE USU."TipoEntidad" = 'EMP' 
and USU."IdUsuario" IN (:p_IdUsuario) 
and USU."IdEmpresa" = :p_IdEmpresa