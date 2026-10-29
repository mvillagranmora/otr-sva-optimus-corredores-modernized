UPDATE "BSCL.SocioNegocio.BD::EJE.Ejecutivo" "eje"
	SET "eje"."IdEmpleadoSupervisor" = :IdEmpleadoSupervisor
		WHERE "IdEmpleadoEjecutivo" = :IdEmpleadoEjecutivo