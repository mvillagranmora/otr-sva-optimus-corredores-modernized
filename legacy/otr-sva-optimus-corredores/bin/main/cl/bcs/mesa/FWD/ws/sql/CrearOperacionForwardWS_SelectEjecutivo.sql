SELECT
EMPLEADO."IdPersona"
,EMPLEADO."IdEmpleado"
,USUARIO."IdUsuario"
FROM "BSCL.Plataforma.BD::ACC.Usuario" AS USUARIO
INNER JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMPLEADO ON USUARIO."IdEntidad" = EMPLEADO."IdEmpleado"
WHERE USUARIO."IdUsuario" = :IdUsuario AND USUARIO."TipoEntidad" = 'EMP';