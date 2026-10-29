-- Obtiene desde tabla SolicitudCambio las solicitudes de cambio a los clientes
SELECT
   SOL."IdSolicitudCambio",
   SOL."CampoModificado",   
   SOL."ValorAntiguo",   
   SOL."ValorNuevo",
   CAST(SOL."IdEntidadOrigen" AS BIGINT) AS "IdEntidadOrigenLong",
   SOL."IdTipoEntidad",
   SOL."IdEstadoAutorizacion"
FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" AS SOL
WHERE SOL."IdTipoEntidad" = :IdTipoEntidad
AND SOL."IdEstadoAutorizacion"	= 'PEN'
AND SOL."IdEntidadOrigen" = :IdEntidadOrigen;