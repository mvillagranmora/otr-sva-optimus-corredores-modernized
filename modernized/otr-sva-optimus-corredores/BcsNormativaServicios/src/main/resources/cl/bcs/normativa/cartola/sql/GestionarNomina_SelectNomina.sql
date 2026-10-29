SELECT
"IdNomina"
,"Certificada"
,"IdUsuarioCertificador"
,"NombreUsuarioCertificador"
,"NotaCertificacion"
,"FechaCertificacion"
,"EstadoProceso"
,"FechaInicioProceso"
,"FechaTerminoProceso"
,"MensajeProceso"
FROM "BSCL.Normativa.BD::CAR.Nomina"
WHERE "IdNomina" = :IdNomina