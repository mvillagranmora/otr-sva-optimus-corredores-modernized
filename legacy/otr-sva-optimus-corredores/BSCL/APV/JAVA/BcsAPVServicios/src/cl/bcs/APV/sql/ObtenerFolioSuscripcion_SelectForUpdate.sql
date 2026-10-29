SELECT
	"Canal",
	"Folio"
FROM
	"BSCL.APV.BD::SCS.FolioSuscripcion"
WHERE
	"Canal" = :p_Canal;