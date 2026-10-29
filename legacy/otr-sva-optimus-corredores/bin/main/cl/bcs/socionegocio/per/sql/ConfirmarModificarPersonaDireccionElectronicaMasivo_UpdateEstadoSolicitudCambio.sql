-- Actualiza el estado de la Solicitud de Cambio	

UPDATE SOL
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" AS SOL 
INNER JOIN "BSCL.SocioNegocio.BD::GBL.ConfirmarModificarPersonaDireccionElectronicaMasivo_SolicitudCambio" AS E ON E."IdEntidadOrigen" = SOL."IdEntidadOrigen" and E."IdTipoEntidad" = SOL."IdTipoEntidad"
AND SOL."IdEstadoAutorizacion" IN ('PEN', 'ING');