UPDATE "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
SET
	EMP."IdEstado" = COALESCE(
		(SELECT SOL."ValorNuevo"
		 FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
		 WHERE SOL."IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
		   AND SOL."IdTipoEntidad" = 'EMPLEADO-ESTADO'
		   AND SOL."CampoModificado" = 'IdEstado'
		   AND SOL."IdEstadoAutorizacion" = 'PEN'),
		EMP."IdEstado"),
	EMP."IdTipoBloqueo" = COALESCE(
		(SELECT SOL."ValorNuevo"
		 FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
		 WHERE SOL."IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
		   AND SOL."IdTipoEntidad" = 'EMPLEADO-ESTADO'
		   AND SOL."CampoModificado" = 'IdTipoBloqueo'
		   AND SOL."IdEstadoAutorizacion" = 'PEN'),
		EMP."IdTipoBloqueo"),
	EMP."IdTipoDesbloqueo" = COALESCE(
		(SELECT SOL."ValorNuevo"
		 FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
		 WHERE SOL."IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
		   AND SOL."IdTipoEntidad" = 'EMPLEADO-ESTADO'
		   AND SOL."CampoModificado" = 'IdTipoDesbloqueo'
		   AND SOL."IdEstadoAutorizacion" = 'PEN'),
		EMP."IdTipoDesbloqueo"),
	EMP."FechaBloqueo" = CASE
		WHEN EXISTS (
			SELECT 1 FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
			WHERE SOL."IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
			  AND SOL."IdTipoEntidad" = 'EMPLEADO-ESTADO'
			  AND SOL."CampoModificado" = 'FechaBloqueo'
			  AND SOL."IdEstadoAutorizacion" = 'PEN')
		THEN CURRENT_UTCTIMESTAMP
		ELSE EMP."FechaBloqueo"
	END,
	EMP."FechaDesbloqueo" = CASE
		WHEN EXISTS (
			SELECT 1 FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
			WHERE SOL."IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
			  AND SOL."IdTipoEntidad" = 'EMPLEADO-ESTADO'
			  AND SOL."CampoModificado" = 'FechaDesbloqueo'
			  AND SOL."IdEstadoAutorizacion" = 'PEN')
		THEN CURRENT_UTCTIMESTAMP
		ELSE EMP."FechaDesbloqueo"
	END,
	EMP."ComentarioBloqueo" = COALESCE(
		(SELECT SOL."ValorNuevo"
		 FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
		 WHERE SOL."IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
		   AND SOL."IdTipoEntidad" = 'EMPLEADO-ESTADO'
		   AND SOL."CampoModificado" = 'ComentarioBloqueo'
		   AND SOL."IdEstadoAutorizacion" = 'PEN'),
		EMP."ComentarioBloqueo"),
	EMP."ComentarioDesbloqueo" = COALESCE(
		(SELECT SOL."ValorNuevo"
		 FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
		 WHERE SOL."IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
		   AND SOL."IdTipoEntidad" = 'EMPLEADO-ESTADO'
		   AND SOL."CampoModificado" = 'ComentarioDesbloqueo'
		   AND SOL."IdEstadoAutorizacion" = 'PEN'),
		EMP."ComentarioDesbloqueo"),
	EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE EMP."IdEmpleado" = :IdEmpleado
