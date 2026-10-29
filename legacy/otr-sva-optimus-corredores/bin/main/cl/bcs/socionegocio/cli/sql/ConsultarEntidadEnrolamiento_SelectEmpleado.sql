SELECT 
'S' AS "EntidadExiste"
, "IdEmpleado" AS "IdEntidad"
FROM "BSCL.SocioNegocio.BD::EMP.Empleado" 
WHERE "IdPersona" = :IdPersona AND "IdEmpresa" = :IdEmpresa;