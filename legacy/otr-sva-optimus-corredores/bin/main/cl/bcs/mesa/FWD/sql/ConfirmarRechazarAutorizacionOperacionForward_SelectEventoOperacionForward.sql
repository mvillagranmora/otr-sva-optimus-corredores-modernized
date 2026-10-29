SELECT
	Clasificacion."IdTrabajo"
	, Clasificacion."IdOperacion"
	, Clasificacion."NumeroOperacionConcepto"
	, Clasificacion."IdTipoEvento"
	, Evento."IdEvento"
    , Evento."IdEstado" AS "IdEstado"
	, '' AS "IdEstadoPosteriorAutorizacion"
FROM #p_EventosOperacionForward Clasificacion
INNER JOIN "BSCL.Plataforma.BD::AUT.Evento" Evento ON (
    Evento."NumeroOperacionConcepto" = Clasificacion."NumeroOperacionConcepto"
	AND Evento."IdTipoEvento" = Clasificacion."IdTipoEvento"
	AND Evento."IdEmpresa" = Clasificacion."IdEmpresa"
	AND Evento."IdTipoOperacion" = Clasificacion."IdTipoOperacion"
	AND TO_NVARCHAR(Evento."Fecha",'YYYY-MM-DD') = TO_NVARCHAR(Clasificacion."Fecha",'YYYY-MM-DD')
)
WHERE Clasificacion."IdTrabajo" = :IdTrabajo AND Evento."IdEstado" = 'VIG'
