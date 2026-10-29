-- Select Datos Solicitud Cambio
SELECT 
    SOL."IdSolicitudCambio",
    SOL."IdEntidadOrigen",
    SOL."IdTipoEntidad",
    SOL."CampoModificado",
    SOL."ValorAntiguo",
    SOL."ValorNuevo",
    CAST(SOL."IdEntidadOrigen" AS BIGINT) AS "IdPortafolio"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
WHERE CAST(SOL."IdEntidadOrigen" AS BIGINT) IN (:IdPortafolios)
  AND SOL."IdTipoEntidad" = :IdTipoEntidad
  AND SOL."IdEstadoAutorizacion" = :IdEstadoAutorizacion
ORDER BY SOL."IdSolicitudCambio"
