-- Actualiza el estado de la Solicitud de Cambio			
UPDATE  SOL
SET     SOL."IdEstadoAutorizacion" = :IdEstadoAutorizacion,
		SOL."FechaRevision" = CURRENT_UTCTIMESTAMP
FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" SOL
WHERE SOL."IdSolicitudCambio" 		= :IdSolicitudCambio
AND SOL."IdEstadoAutorizacion" IN ('PEN');