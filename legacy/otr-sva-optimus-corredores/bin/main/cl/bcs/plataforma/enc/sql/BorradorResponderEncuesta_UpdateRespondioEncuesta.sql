UPDATE "BSCL.SocioNegocio.BD::ENR.Enrolamiento" E
SET E."RespondioEncuestaInversionista" = 'S'
WHERE E."IdEnrolamiento" = :IdEnrolamiento;