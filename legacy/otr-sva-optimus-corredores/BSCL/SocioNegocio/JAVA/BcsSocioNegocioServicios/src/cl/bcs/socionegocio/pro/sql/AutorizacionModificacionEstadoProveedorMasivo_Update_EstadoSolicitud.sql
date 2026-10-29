-- Actualizar estado de SolicitudCambio (AUT/ACT para autorizar, RZD para rechazar)
UPDATE "BSCL.Plataforma.BD::PLA.SolicitudCambio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdEntidadOrigen" = CAST(:IdProveedor AS NVARCHAR(40))
	AND "IdTipoEntidad" = 'PROVEEDOR-ESTADO'
	AND "IdEstadoAutorizacion" = 'PEN';
