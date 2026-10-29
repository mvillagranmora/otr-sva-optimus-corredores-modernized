--Actualiza a RZD el IdEstadoAutorizacion
UPDATE 	SOL
SET     SOL."IdEstadoAutorizacion" = :IdEstadoAutorizacion,
		SOL."FechaRevision" = CURRENT_UTCTIMESTAMP
FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" SOL
WHERE SOL."IdSolicitudCambio" 		= :IdSolicitudCambio
AND   SOL."IdEstadoAutorizacion"	= 'PEN';