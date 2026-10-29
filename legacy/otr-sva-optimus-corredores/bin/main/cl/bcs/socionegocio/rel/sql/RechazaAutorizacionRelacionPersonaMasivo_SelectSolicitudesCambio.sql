SELECT 
	"IdSolicitudCambio" AS "IdSolicitudCambio",
	"IdEntidadOrigen" AS "IdEntidadOrigen",
	"IdTipoEntidad" AS "IdTipoEntidad",
	"IdEstadoAutorizacion" AS "IdEstadoAutorizacion"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
WHERE SOL."IdEntidadOrigen" = :p_IdEntidadOrigen
  AND SOL."IdTipoEntidad" = :p_IdTipoEntidad
  AND SOL."IdEstadoAutorizacion" = 'PEN'
