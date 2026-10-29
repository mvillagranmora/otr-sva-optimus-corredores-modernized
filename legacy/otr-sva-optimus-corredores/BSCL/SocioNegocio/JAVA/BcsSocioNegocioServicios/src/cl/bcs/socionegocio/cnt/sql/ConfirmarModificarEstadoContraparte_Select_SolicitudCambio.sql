SELECT
	"IdSolicitudCambio" AS "IdSolicitudCambio",
	"IdEntidadOrigen" AS "IdEntidadOrigen",
	"IdTipoEntidad" AS "IdTipoEntidad",
	"CampoModificado" AS "CampoModificado",
	"TipoDato" AS "TipoDato",
	"ValorAntiguo" AS "ValorAntiguo",
	"ValorNuevo" AS "ValorNuevo",
	"IdEstadoAutorizacion" AS "IdEstadoAutorizacion"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio"
WHERE "IdEntidadOrigen" = :IdEntidadOrigen
	AND "IdTipoEntidad" = :IdTipoEntidad
	AND "IdEstadoAutorizacion" = 'PEN'
