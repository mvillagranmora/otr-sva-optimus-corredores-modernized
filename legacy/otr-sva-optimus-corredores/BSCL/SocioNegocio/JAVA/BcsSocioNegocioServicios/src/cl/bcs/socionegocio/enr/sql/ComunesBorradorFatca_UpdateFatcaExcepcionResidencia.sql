UPDATE "BSCL.SocioNegocio.BD::ENR.Fatca" 
SET "ExcepcionResidente" = :ExcepcionResidencia
WHERE "IdEnrolamiento" = :IdEnrolamiento;