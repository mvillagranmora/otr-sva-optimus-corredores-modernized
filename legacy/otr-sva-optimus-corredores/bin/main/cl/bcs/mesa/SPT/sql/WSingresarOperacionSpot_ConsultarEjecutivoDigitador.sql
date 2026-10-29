SELECT "IdEmpleado" AS "IdEmpleadoDigitador", EMP."CodigoEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP 
LEFT JOIN "BSCL.SocioNegocio.BD::EJE.Ejecutivo" EJE ON EJE."IdEmpleadoEjecutivo" = EMP."IdEmpleado" 
WHERE EMP."IdEstado" = 'ACT' AND EJE."IdEstado" = 'ACT' AND EMP."CodigoEmpleado" IN (:CodigoEjecutivoDigitador);