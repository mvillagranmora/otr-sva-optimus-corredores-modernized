UPDATE "BSCL.SocioNegocio.BD::SOC.Enrolamiento"
	SET "IdEntidadEnrolada" = :p_IdPersona
WHERE "IdEnrolamiento" = :p_IdEnrolamiento;