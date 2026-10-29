-- Actualiza Empleado solo cuando existe un cambio de estado a muerte (BMUERT) en SolicitudCambio
UPDATE "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP
SET
	EMP."IdEstado" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdEstado'),
		EMP."IdEstado"),
	EMP."IdTipoBloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdTipoBloqueo'),
		EMP."IdTipoBloqueo"),
	EMP."IdTipoDesbloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdTipoDesbloqueo'),
		EMP."IdTipoDesbloqueo"),
	EMP."FechaBloqueo" = CASE
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'FechaBloqueo')
		THEN CURRENT_UTCTIMESTAMP
		ELSE EMP."FechaBloqueo"
	END,
	EMP."FechaDesbloqueo" = CASE
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'FechaDesbloqueo')
		THEN CURRENT_UTCTIMESTAMP
		ELSE EMP."FechaDesbloqueo"
	END,
	EMP."ComentarioBloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'ComentarioBloqueo'),
		EMP."ComentarioBloqueo"),
	EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE EMP."IdPersona" = :IdPersona
  AND EXISTS (
	SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
	WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
	  AND "IdTipoEntidad" = 'PERSONA-ESTADO'
	  AND "IdEstadoAutorizacion" = 'PEN'
	  AND "CampoModificado" = 'IdEstado'
	  AND "ValorNuevo" = 'BMUERT'
  );
