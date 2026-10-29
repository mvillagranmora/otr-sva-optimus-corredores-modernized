UPDATE "BSCL.SocioNegocio.BD::EJE.Ejecutivo"
SET "IdEmpleadoReemplaza" = NULL,
    "FechaInicioReemplazo" = '0001-01-01 00:00:00',
    "FechaFinReemplazo" = '0001-01-01 00:00:00'
WHERE "IdEmpleadoEjecutivo" = :IdEmpleadoEjecutivo;
