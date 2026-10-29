INSERT INTO #p_EventosOperacionSpot (
	"IdTrabajo" 
	, "IdOperacionSpot"
	, "NumeroOperacionConcepto"
	, "IdTipoEvento"
	, "IdEmpresa"
	, "IdTipoOperacion"
	, "Fecha"
) VALUES (
	:IdTrabajo
	, :IdOperacionSpot
	, :NumeroOperacionConcepto
	, :IdTipoEvento
	, :IdEmpresa
	, :IdTipoOperacion
	, :Fecha
)