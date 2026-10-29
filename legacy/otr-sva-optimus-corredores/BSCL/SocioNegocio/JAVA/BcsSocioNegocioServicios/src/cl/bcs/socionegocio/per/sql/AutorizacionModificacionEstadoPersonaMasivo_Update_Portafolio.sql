-- Propaga cambios de estado de Persona hacia Portafolio a través de Cliente
-- Maneja la lógica de bloqueo/desbloqueo inversa (limpiar el campo opuesto)
UPDATE POR
SET
	POR."IdEstado" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdEstado'),
		POR."IdEstado"),
	POR."IdTipoBloqueo" = CASE
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'IdTipoBloqueo')
		THEN (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		      WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		        AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		        AND "IdEstadoAutorizacion" = 'PEN'
		        AND "CampoModificado" = 'IdTipoBloqueo')
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'IdTipoDesbloqueo')
		THEN ''
		ELSE POR."IdTipoBloqueo"
	END,
	POR."IdTipoDesbloqueo" = CASE
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'IdTipoDesbloqueo')
		THEN (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		      WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		        AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		        AND "IdEstadoAutorizacion" = 'PEN'
		        AND "CampoModificado" = 'IdTipoDesbloqueo')
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'IdTipoBloqueo')
		THEN ''
		ELSE POR."IdTipoDesbloqueo"
	END,
	POR."FechaBloqueo" = CASE
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'FechaBloqueo')
		THEN CURRENT_UTCTIMESTAMP
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'FechaDesbloqueo')
		THEN TO_DATE('0001-01-01', 'YYYY-MM-DD')
		ELSE POR."FechaBloqueo"
	END,
	POR."FechaDesbloqueo" = CASE
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'FechaDesbloqueo')
		THEN CURRENT_UTCTIMESTAMP
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'FechaBloqueo')
		THEN TO_DATE('0001-01-01', 'YYYY-MM-DD')
		ELSE POR."FechaDesbloqueo"
	END,
	POR."ComentarioBloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'ComentarioBloqueo'),
		POR."ComentarioBloqueo"),
	POR."ComentarioDesbloqueo" = CASE
		WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		             WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
		               AND "IdTipoEntidad" = 'PERSONA-ESTADO'
		               AND "IdEstadoAutorizacion" = 'PEN'
		               AND "CampoModificado" = 'ComentarioBloqueo')
		THEN ''
		ELSE POR."ComentarioDesbloqueo"
	END,
	POR."FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"
WHERE CLI."IdPersona" = :IdPersona;
