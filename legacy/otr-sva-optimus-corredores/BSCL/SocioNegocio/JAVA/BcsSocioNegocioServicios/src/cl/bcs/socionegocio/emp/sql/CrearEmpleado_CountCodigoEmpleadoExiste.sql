SELECT COUNT(1) AS "Contador"
FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"
WHERE "CodigoEmpleado" = :CodigoEmpleado AND "IdEmpresa" = :IdEmpresa
