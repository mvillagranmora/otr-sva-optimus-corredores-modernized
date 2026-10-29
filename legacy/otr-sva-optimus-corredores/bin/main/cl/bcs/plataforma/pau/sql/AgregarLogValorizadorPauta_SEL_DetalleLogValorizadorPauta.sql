SELECT
	"IdDetalleLogValorizadorPauta",
	"IdLogValorizadorPauta",
	"FolioCartera",
	"Nemotecnico",
	"Estado",
	"Observacion",
	"Origen"
FROM "BSCL.Plataforma.BD::PLA.DetalleLogValorizadorPauta"
WHERE "IdLogValorizadorPauta" = :IdLogValorizadorPauta
ORDER BY "IdDetalleLogValorizadorPauta"