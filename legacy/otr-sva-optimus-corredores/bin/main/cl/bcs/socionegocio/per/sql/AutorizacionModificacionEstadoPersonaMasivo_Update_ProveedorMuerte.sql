-- Actualiza Proveedor solo cuando existe un cambio de estado a muerte (BMUERT) en SolicitudCambio
UPDATE "BSCL.SocioNegocio.BD::PRO.Proveedor" AS PRO
SET
	PRO."IdEstado" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdEstado'),
		PRO."IdEstado"),
	PRO."IdTipoBloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdTipoBloqueo'),
		PRO."IdTipoBloqueo"),
	PRO."IdTipoDesbloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdTipoDesbloqueo'),
		PRO."IdTipoDesbloqueo"),
	PRO."FechaBloqueo" = CASE
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'FechaBloqueo')
		THEN CURRENT_UTCTIMESTAMP
		ELSE PRO."FechaBloqueo"
	END,
	PRO."FechaDesbloqueo" = CASE
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'FechaDesbloqueo')
		THEN CURRENT_UTCTIMESTAMP
		ELSE PRO."FechaDesbloqueo"
	END,
	PRO."FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE PRO."IdPersona" = :IdPersona
  AND EXISTS (
	SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
	WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
	  AND "IdTipoEntidad" = 'PERSONA-ESTADO'
	  AND "IdEstadoAutorizacion" = 'PEN'
	  AND "CampoModificado" = 'IdEstado'
	  AND "ValorNuevo" = 'BMUERT'
  );
