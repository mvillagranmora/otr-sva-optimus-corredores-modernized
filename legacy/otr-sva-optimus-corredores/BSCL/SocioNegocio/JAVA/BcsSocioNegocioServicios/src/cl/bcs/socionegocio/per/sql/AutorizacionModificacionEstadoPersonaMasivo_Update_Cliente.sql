-- Propaga cambios de estado de Persona hacia Cliente
-- Actualiza solo los campos con cambios pendientes en SolicitudCambio
UPDATE CLI
SET
	CLI."IdEstado" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdEstado'),
		CLI."IdEstado"),
	CLI."IdTipoBloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdTipoBloqueo'),
		CLI."IdTipoBloqueo"),
	CLI."IdTipoDesbloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdTipoDesbloqueo'),
		CLI."IdTipoDesbloqueo"),
	CLI."FechaBloqueo" = CASE
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'FechaBloqueo')
		THEN CURRENT_UTCTIMESTAMP
		ELSE CLI."FechaBloqueo"
	END,
	CLI."FechaDesbloqueo" = CASE
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'FechaDesbloqueo')
		THEN CURRENT_UTCTIMESTAMP
		ELSE CLI."FechaDesbloqueo"
	END,
	CLI."ComentarioBloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'ComentarioBloqueo'),
		CLI."ComentarioBloqueo"),
	CLI."FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
WHERE CLI."IdPersona" = :IdPersona;
