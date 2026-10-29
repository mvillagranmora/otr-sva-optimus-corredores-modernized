SELECT 
    SOL."IdEntidadOrigen" AS "IdEntidadOrigen",
    SOL."IdTipoEntidad" AS "IdTipoEntidad"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
WHERE SOL."IdEntidadOrigen" IN (:IdEntidadOrigen)
    AND SOL."IdTipoEntidad" = :IdTipoEntidad
    AND SOL."IdEstadoAutorizacion" = 'PEN'
