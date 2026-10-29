INSERT INTO "BSCL.Plataforma.BD::PLA.LogValorizadorPauta" (
	"IdLogValorizadorPauta",
	"IdEjecucion",
	"FechaInicio",
	"FechaTermino",
	"Proceso",
	"Mercado",
	"RutaLog"
)
VALUES (
	:IdLogValorizadorPauta,
	:IdEjecucion,
	CURRENT_TIMESTAMP,
	NULL,
	:Proceso,
	:Mercado,
	NULL
)