INSERT INTO #p_EventosOperacionForward (
	"IdTrabajo" 
	, "IdOperacion"
	, "NumeroOperacionConcepto"
	, "IdTipoEvento"
	, "IdEmpresa"
	, "IdTipoOperacion"
	, "Fecha"
) VALUES (
	:IdTrabajo
	, :IdOperacion
	, :NumeroOperacionConcepto
	, :IdTipoEvento
	, :IdEmpresa
	, :IdTipoOperacion
	, :Fecha
)