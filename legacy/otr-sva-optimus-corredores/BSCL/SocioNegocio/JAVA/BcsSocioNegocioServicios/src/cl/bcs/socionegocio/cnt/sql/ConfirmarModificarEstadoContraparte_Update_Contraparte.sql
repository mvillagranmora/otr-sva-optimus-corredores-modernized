-- Actualiza campos de estado de Contraparte con los valores pendientes en SolicitudCambio (CONTRAPARTE-ESTADO)
UPDATE "BSCL.SocioNegocio.BD::CNT.Contraparte" AS TGT
SET
	"IdEstado" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdContraparte AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'CONTRAPARTE-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdEstado'),
		TGT."IdEstado"),
	"IdTipoBloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdContraparte AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'CONTRAPARTE-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdTipoBloqueo'),
		TGT."IdTipoBloqueo"),
	"IdTipoDesbloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdContraparte AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'CONTRAPARTE-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdTipoDesbloqueo'),
		TGT."IdTipoDesbloqueo"),
	"FechaBloqueo" = CASE
		WHEN EXISTS (
			SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
			WHERE "IdEntidadOrigen" = CAST(:IdContraparte AS NVARCHAR(40))
			  AND "IdTipoEntidad" = 'CONTRAPARTE-ESTADO'
			  AND "IdEstadoAutorizacion" = 'PEN'
			  AND "CampoModificado" = 'FechaBloqueo'
		)
		THEN CURRENT_UTCTIMESTAMP
		ELSE TGT."FechaBloqueo"
	END,
	"FechaDesbloqueo" = CASE
		WHEN EXISTS (
			SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
			WHERE "IdEntidadOrigen" = CAST(:IdContraparte AS NVARCHAR(40))
			  AND "IdTipoEntidad" = 'CONTRAPARTE-ESTADO'
			  AND "IdEstadoAutorizacion" = 'PEN'
			  AND "CampoModificado" = 'FechaDesbloqueo'
		)
		THEN CURRENT_UTCTIMESTAMP
		ELSE TGT."FechaDesbloqueo"
	END,
	"ComentarioBloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdContraparte AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'CONTRAPARTE-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'ComentarioBloqueo'),
		TGT."ComentarioBloqueo"),
	"ComentarioDesbloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdContraparte AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'CONTRAPARTE-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'ComentarioDesbloqueo'),
		TGT."ComentarioDesbloqueo"),
	"FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE TGT."IdContraparte" = :IdContraparte
