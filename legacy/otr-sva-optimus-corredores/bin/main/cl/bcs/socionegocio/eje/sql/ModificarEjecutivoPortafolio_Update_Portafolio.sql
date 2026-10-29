UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET "IdEmpleadoEjecutivo" = :IdEmpleadoEjecutivo,
    "EsEjecutivoExclusivo" = :EsEjecutivoExclusivo
WHERE "IdPortafolio" = :IdPortafolio
AND "IdCliente" = :IdCliente
