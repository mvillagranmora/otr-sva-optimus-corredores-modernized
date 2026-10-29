update "BSCL.SocioNegocio.BD::SOC.Enrolamiento" 
	SET "IdEntidadEnrolada" = :p_IdPersona
where "IdEnrolamiento" = :p_IdEnrolamiento;