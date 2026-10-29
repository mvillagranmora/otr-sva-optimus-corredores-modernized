UPDATE "BSCL.SocioNegocio.BD::EJE.Ejecutivo"
SET "IdEmpleadoReemplaza" = :IdEmpleadoReemplazo,
    "FechaInicioReemplazo" = :FechaInicioReemplazo,
    "FechaFinReemplazo" = :FechaFinReemplazo
WHERE "IdEmpleadoEjecutivo" = :IdEmpleadoEjecutivo;
