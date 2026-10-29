SELECT
"IdPersona" 
,"IdEmpleado" AS "IdEmpleadoEjecutivo"
FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
WHERE "IdEmpleado" IN (:IdEmpleadoEjecutivo)