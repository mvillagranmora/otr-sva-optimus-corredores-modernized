-- Actualiza los campos de estado de Persona con valores de SolicitudCambio (PERSONA-ESTADO)
-- Usa COALESCE para actualizar solo los campos con cambios pendientes
UPDATE "BSCL.SocioNegocio.BD::PER.Persona" AS TGT
SET
	"IdEstado" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdEstado'),
		TGT."IdEstado"),
	"IdTipoBloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdTipoBloqueo'),
		TGT."IdTipoBloqueo"),
	"IdTipoDesbloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdTipoDesbloqueo'),
		TGT."IdTipoDesbloqueo"),
	"FechaBloqueo" = CASE
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'FechaBloqueo')
		THEN CURRENT_UTCTIMESTAMP
		ELSE TGT."FechaBloqueo"
	END,
	"FechaDesbloqueo" = CASE
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'FechaDesbloqueo')
		THEN CURRENT_UTCTIMESTAMP
		ELSE TGT."FechaDesbloqueo"
	END,
	"FechaDefuncion" = CASE
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'FechaBloqueo')
		     AND COALESCE(
		         (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		          WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		            AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		            AND "IdEstadoAutorizacion" = 'PEN'
		            AND "CampoModificado" = 'IdTipoBloqueo'),
		         TGT."IdTipoBloqueo") = 'BMUERT'
		THEN (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		      WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		        AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		        AND "IdEstadoAutorizacion" = 'PEN'
		        AND "CampoModificado" = 'FechaBloqueo')
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'FechaDesbloqueo')
		THEN ''
		ELSE TGT."FechaDefuncion"
	END,
	"ComentarioBloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'ComentarioBloqueo'),
		TGT."ComentarioBloqueo"),
	"ComentarioDesbloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'ComentarioDesbloqueo'),
		TGT."ComentarioDesbloqueo"),
	"FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE TGT."IdPersona" = :IdPersona;
