SELECT 
	"CampoModificado",
	"ValorAntiguo",
	"ValorNuevo"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio"
WHERE "IdTipoEntidad" = :IdTipoEntidad
	AND "IdEntidadOrigen" = :IdEntidadOrigen
	AND "IdEstadoAutorizacion" = 'PEN'
ORDER BY "IdSecuenciaCambio"
