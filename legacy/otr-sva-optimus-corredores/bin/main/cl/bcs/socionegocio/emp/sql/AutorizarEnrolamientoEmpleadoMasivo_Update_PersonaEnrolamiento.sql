UPDATE "BSCL.SocioNegocio.BD::PER.Persona"
SET "EsEnrolado" = 'S'
WHERE "IdPersona" IN (
    SELECT EMP."IdPersona"
    FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
    WHERE EMP."IdEmpleado" = :IdEmpleado
)
AND "EsEnrolado" IN ('P', 'N')
