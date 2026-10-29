SELECT SUP."IdEmpleadoSupervisor"
FROM "BSCL.Plataforma.BD::ACC.Usuario" USU
INNER JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
    ON EMP."IdEmpleado" = USU."IdEntidad"
INNER JOIN "BSCL.SocioNegocio.BD::EJE.Supervisor" SUP
    ON SUP."IdEmpleadoSupervisor" = EMP."IdEmpleado"
WHERE USU."IdUsuario" = :IdUsuario
  AND USU."TipoEntidad" = 'EMP'
  AND SUP."IdEstado" = 'ACT'
LIMIT 1;