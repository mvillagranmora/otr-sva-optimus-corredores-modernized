SELECT "EMP"."IdSucursal" as "IdSucursalEjecutivo","EMP"."CodigoEmpleado" as "CodigoEmpleadoEjecutivo", "EMP"."NombreCompleto" AS "NombreEmpleadoEjecutivo"
FROM "BSCL.SocioNegocio.BD::EMP.Empleado" AS "EMPLEADO"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS "EMP"  ON("EMP"."IdPersona" = "EMPLEADO"."IdPersona") 
WHERE "EMPLEADO"."IdEmpleado" IN (:p_IdEmpleado);