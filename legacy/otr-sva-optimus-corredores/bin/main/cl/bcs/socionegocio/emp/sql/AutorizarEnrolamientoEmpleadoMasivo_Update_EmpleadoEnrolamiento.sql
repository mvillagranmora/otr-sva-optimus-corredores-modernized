UPDATE "BSCL.SocioNegocio.BD::EMP.Empleado"
SET "EsEnrolado" = :EsEnrolado
WHERE "IdEmpleado" = :IdEmpleado
  AND (
    (:EsEnrolado = 'S' AND "EsEnrolado" IN ('P', 'N'))  -- Confirmar: solo si está Pendiente o No enrolado
    OR 
    (:EsEnrolado = 'N' AND "EsEnrolado" = 'P')          -- Rechazar: solo si está Pendiente
  )
