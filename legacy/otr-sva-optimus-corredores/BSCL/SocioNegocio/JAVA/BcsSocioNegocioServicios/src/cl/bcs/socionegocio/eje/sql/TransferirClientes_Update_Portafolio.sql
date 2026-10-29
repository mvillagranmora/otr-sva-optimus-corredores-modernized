UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET "IdEmpleadoEjecutivo" = :IdEmpleadoEjecutivoNuevo
WHERE "IdEmpleadoEjecutivo" = :IdEmpleadoEjecutivoAntiguo;
