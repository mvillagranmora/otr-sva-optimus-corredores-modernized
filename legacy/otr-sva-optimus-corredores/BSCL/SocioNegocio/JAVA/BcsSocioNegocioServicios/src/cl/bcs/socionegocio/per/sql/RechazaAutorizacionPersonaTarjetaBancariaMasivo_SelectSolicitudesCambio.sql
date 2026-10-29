SELECT 
	"IdSolicitudCambio",
	"IdEntidadOrigen",
	"IdTipoEntidad",
	"IdEstadoAutorizacion"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio"
WHERE "IdEntidadOrigen" = :p_IdEntidadOrigen
	AND "IdTipoEntidad" = :p_IdTipoEntidad
	AND "IdEstadoAutorizacion" = 'PEN'
