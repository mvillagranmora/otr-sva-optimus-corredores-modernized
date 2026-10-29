INSERT INTO "BSCL.Plataforma.BD::PAU.EjecucionDetalle"
(
	"IdEjecucion",
	"CodigoProceso",
	"FechaInicio",
	"FechaFin",
	"Estado"
)
VALUES (
	:IdEjecucion,
	:CodigoProceso,
	:FechaInicio,
	:FechaFin,
	:Estado
);