SELECT COUNT(CE."IdEmpleado") AS "Contador"
FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" CEJ
    ON CE."IdEmpleado" = CEJ."IdEmpleadoEjecutivo"
WHERE CE."IdEmpleado" = :IdEmpleado
  AND CEJ."IdEstado" = 'ACT'
