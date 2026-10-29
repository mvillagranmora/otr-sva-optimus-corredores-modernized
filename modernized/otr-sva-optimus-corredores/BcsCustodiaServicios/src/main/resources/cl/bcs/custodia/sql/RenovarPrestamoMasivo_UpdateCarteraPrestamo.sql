UPDATE
	"BSCL.Custodia.BD::CUS.Cartera" Cartera
SET
	Cartera."IdEstado" = :IdEstado
	, Cartera."FolioCarteraRelacionada" = :FolioCarteraRelacionada
WHERE
	Cartera."IdCartera" = :IdCartera;
	