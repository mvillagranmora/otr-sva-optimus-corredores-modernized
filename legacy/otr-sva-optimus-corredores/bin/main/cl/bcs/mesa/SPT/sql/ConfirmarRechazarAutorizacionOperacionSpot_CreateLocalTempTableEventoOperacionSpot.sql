CREATE LOCAL TEMPORARY TABLE #p_EventosOperacionSpot (
	"IdTrabajo" BIGINT NOT NULL
	, "IdOperacionSpot" BIGINT NOT NULL
	, "NumeroOperacionConcepto" NVARCHAR(40) NOT NULL
	, "IdTipoEvento" NVARCHAR(6) NOT NULL
	, "IdEmpresa" BIGINT NOT NULL
	, "IdTipoOperacion" NVARCHAR(6) NOT NULL
	, "Fecha" DATE NOT NULL
)