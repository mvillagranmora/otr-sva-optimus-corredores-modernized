-- Actualiza Contraparte solo cuando existe un cambio de estado a muerte (BMUERT) en SolicitudCambio
UPDATE "BSCL.SocioNegocio.BD::CNT.Contraparte" AS CNT
SET
	CNT."IdEstado" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdEstado'),
		CNT."IdEstado"),
	CNT."IdTipoBloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdTipoBloqueo'),
		CNT."IdTipoBloqueo"),
	CNT."IdTipoDesbloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdTipoDesbloqueo'),
		CNT."IdTipoDesbloqueo"),
	CNT."FechaBloqueo" = CASE
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'FechaBloqueo')
		THEN CURRENT_UTCTIMESTAMP
		ELSE CNT."FechaBloqueo"
	END,
	CNT."FechaDesbloqueo" = CASE
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'FechaDesbloqueo')
		THEN CURRENT_UTCTIMESTAMP
		ELSE CNT."FechaDesbloqueo"
	END,
	CNT."ComentarioBloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'ComentarioBloqueo'),
		CNT."ComentarioBloqueo"),
	CNT."FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE CNT."IdPersona" = :IdPersona
  AND EXISTS (
	SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
	WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
	  AND "IdTipoEntidad" = 'PERSONA-ESTADO'
	  AND "IdEstadoAutorizacion" = 'PEN'
	  AND "CampoModificado" = 'IdEstado'
	  AND "ValorNuevo" = 'BMUERT'
  );
