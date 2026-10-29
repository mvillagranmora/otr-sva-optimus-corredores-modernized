-- Actualizar estado de SolicitudCambio por IdSolicitudCambio
UPDATE "BSCL.Plataforma.BD::PLA.SolicitudCambio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdSolicitudCambio" = :IdSolicitudCambio
