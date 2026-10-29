INSERT INTO "BSCL.Plataforma.BD::PAU.EjecucionDetalle"
(
	"FechaSistema",
	"IdEmpresa",
	"CodigoProceso",
	"FechaInicio",
	"FechaFin",
	"IdEjecucion",
	"Estado"
)
VALUES (
	:FechaSistema,
	:IdEmpresa,
	:CodigoProceso,
	:FechaInicio,
	:FechaFin,
	"BSCL.Plataforma.BD.Secuencias::PAU.Ejecucion".NEXTVAL,
	:Estado
);
