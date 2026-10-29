SELECT COUNT(1) as "Contador"
FROM "BSCL.SocioNegocio.BD::EJE.Ejecutivo"
WHERE "IdEmpleadoEjecutivo" = :IdEmpleadoEjecutivo
  AND "IdEmpleadoSupervisor" = :IdEmpleadoSupervisor;
