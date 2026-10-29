INSERT INTO #p_EventosOperacionMesa (
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