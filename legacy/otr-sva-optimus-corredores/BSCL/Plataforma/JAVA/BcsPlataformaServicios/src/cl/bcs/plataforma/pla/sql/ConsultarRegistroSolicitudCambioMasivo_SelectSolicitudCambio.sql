SELECT S."IdSolicitudCambio"
	,S."CampoModificado"
	,S."ValorAntiguo"
	,S."ValorNuevo"
	,S."IdEntidadOrigenLong"
	,S."IdTipoEntidad"
	,S."IdEstadoAutorizacion"
FROM (
	SELECT SOLICITUD."IdSolicitudCambio"
		,SOLICITUD."CampoModificado"
		,SOLICITUD."ValorAntiguo"
		,SOLICITUD."ValorNuevo"
		,CAST(SOLICITUD."IdEntidadOrigen" AS BIGINT) AS "IdEntidadOrigenLong"
		,SOLICITUD."IdTipoEntidad"
		,SOLICITUD."IdEstadoAutorizacion"
		,RANK() OVER (PARTITION BY SOLICITUD."IdTipoEntidad", SOLICITUD."IdEntidadOrigen", SOLICITUD."CampoModificado" ORDER BY SOLICITUD."IdSolicitudCambio" DESC) AS "RankSolicitud"
	FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" AS SOLICITUD
	LEFT JOIN "BSCL.Plataforma.BD::PLA.ConsultarSolicitudCambioMasivo" AS CONSULTA ON (SOLICITUD."IdTipoEntidad" = CONSULTA."IdTipoEntidad" AND SOLICITUD."IdEntidadOrigen" = CONSULTA."IdEntidadOrigen")
	WHERE
	SOLICITUD."IdEstadoAutorizacion" = 'PEN' 
	AND CONSULTA."IdTrabajo" = :IdTrabajo
) S
WHERE S."RankSolicitud" = 1;
