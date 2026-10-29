SELECT
	"IdLogValorizadorPauta",
	"FechaInicio",
	"FechaTermino",
	"Proceso",
	"Mercado"
FROM "BSCL.Plataforma.BD::PLA.LogValorizadorPauta"
WHERE "IdLogValorizadorPauta" = :IdLogValorizadorPauta