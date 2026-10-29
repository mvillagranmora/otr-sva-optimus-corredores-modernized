UPDATE "BSCL.Normativa.BD::CAR.Nomina"
SET 
"Certificada" = :Certificada
,"IdUsuarioCertificador" = :IdUsuarioCertificador
,"NombreUsuarioCertificador" = :NombreUsuarioCertificador
,"NotaCertificacion" = :NotaCertificacion
,"FechaCertificacion" = :FechaCertificacion
,"EstadoProceso" = :EstadoProceso
,"FechaInicioProceso" = :FechaInicioProceso
,"FechaTerminoProceso" = :FechaTerminoProceso
,"MensajeProceso" = :MensajeProceso
WHERE "IdNomina" = :IdNomina