SELECT
    Usuario."IdUsuario" AS "IdUsuario"
    , Empleado."IdEmpleado" AS "IdEmpleado"
    , Empleado."CodigoEmpleado" AS "CodigoEmpleado"
FROM "BSCL.Plataforma.BD::ACC.Usuario" Usuario
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" Empleado ON (Empleado."IdEmpleado" = Usuario."IdEntidad")
WHERE Usuario."IdEmpresa" = :IdEmpresa
    AND Usuario."IdUsuario" IN (:IdUsuario)
    AND Empleado."IdEmpleado" IS NOT NULL
    AND Usuario."IdEstado" = 'ACT'
    AND Usuario."TipoEntidad" = 'EMP';