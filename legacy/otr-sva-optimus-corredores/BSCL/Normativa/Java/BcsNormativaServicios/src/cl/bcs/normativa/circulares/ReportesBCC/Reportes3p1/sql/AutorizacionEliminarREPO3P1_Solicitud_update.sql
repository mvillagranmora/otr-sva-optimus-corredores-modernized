	
UPDATE BSCL."BSCL.Plataforma.BD::PLA.SolicitudCambio" SET "IdEstadoAutorizacion"=:IdEstadoAutorizacion
WHERE "IdSolicitudCambio" IN (:IdSolicitudCambio);
