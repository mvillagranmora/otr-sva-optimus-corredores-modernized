-- Patrón modelo: solo actualiza campos si hay cambios pendientes en SolicitudCambio
UPDATE "BSCL.SocioNegocio.BD::PRO.Proveedor" AS TGT
SET
	"IdEstado" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdProveedor AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PROVEEDOR-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdEstado'),
		TGT."IdEstado"),
	"IdTipoBloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdProveedor AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PROVEEDOR-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdTipoBloqueo'),
		TGT."IdTipoBloqueo"),
	"IdTipoDesbloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdProveedor AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PROVEEDOR-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'IdTipoDesbloqueo'),
		TGT."IdTipoDesbloqueo"),
	"FechaBloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdProveedor AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PROVEEDOR-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'FechaBloqueo'),
		TGT."FechaBloqueo"),
	"FechaDesbloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdProveedor AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PROVEEDOR-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'FechaDesbloqueo'),
		TGT."FechaDesbloqueo"),
	"ComentarioBloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdProveedor AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PROVEEDOR-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'ComentarioBloqueo'),
		TGT."ComentarioBloqueo"),
	"ComentarioDesbloqueo" = COALESCE(
		(SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
		 WHERE "IdEntidadOrigen" = CAST(:IdProveedor AS NVARCHAR(40))
		   AND "IdTipoEntidad" = 'PROVEEDOR-ESTADO'
		   AND "IdEstadoAutorizacion" = 'PEN'
		   AND "CampoModificado" = 'ComentarioDesbloqueo'),
		TGT."ComentarioDesbloqueo"),
	"FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE TGT."IdProveedor" = :IdProveedor;
