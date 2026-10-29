INSERT INTO "BSCL.Plataforma.BD::LIM.PlantillaLimite" (
	"IdPlantillaLimite",
	"DescripcionPlantillaLimite",
	"EsPerfilPersonalizado",
	"FechaCreacion",
	"FechaModificacion",
	"FechaIngreso",
	"IdEstadoAutorizacion",
	"IdUsuarioCreador",
	"IdEstado",
	"PorDefecto",
	"IdEmpresa"
)
VALUES (
	:IdPlantillaLimite,
	:DescripcionPlantillaLimite,
	:EsPerfilPersonalizado,
	:FechaCreacion,
	:FechaModificacion,
	:FechaIngreso,
	:IdEstadoAutorizacion,
	:IdUsuarioCreador,
	:IdEstado,
	:PorDefecto,
	:IdEmpresa
)
