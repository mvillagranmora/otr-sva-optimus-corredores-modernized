-- Obtiene desde tabla SolicitudCambio las solicitudes de cambio al fatca de persona
SELECT
   SOL."IdSolicitudCambio",
   SOL."CampoModificado",   
   SOL."ValorAntiguo",   
   SOL."ValorNuevo",
   SOL."IdEntidadOrigen",
   SOL."IdTipoEntidad",
   CAST(SOL."IdEntidadOrigen" AS BIGINT) AS "IdPersona", 
   SOL."IdEstadoAutorizacion"
FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" AS SOL
WHERE SOL."IdTipoEntidad" = :IdTipoEntidad
AND SOL."IdEstadoAutorizacion"	= 'PEN'
AND SOL."IdEntidadOrigen" = :IdEntidadOrigen;