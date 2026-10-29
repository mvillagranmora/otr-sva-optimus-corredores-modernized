SELECT
	SOL."IdSolicitudCambio" AS "IdSolicitudCambio",
	SOL."IdEntidadOrigen" AS "IdEntidadOrigen",
	SOL."IdTipoEntidad" AS "IdTipoEntidad",
	SOL."CampoModificado" AS "CampoModificado",
	SOL."ValorAntiguo" AS "ValorAntiguo",
	SOL."ValorNuevo" AS "ValorNuevo",
	CAST(SOL."IdEntidadOrigen" AS BIGINT) AS "IdPortafolio"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
WHERE CAST(SOL."IdEntidadOrigen" AS BIGINT) IN (:IdPortafolios)
  AND SOL."IdTipoEntidad" = :IdTipoEntidad
  AND SOL."IdEstadoAutorizacion" = :IdEstadoAutorizacion
ORDER BY SOL."IdSolicitudCambio"
