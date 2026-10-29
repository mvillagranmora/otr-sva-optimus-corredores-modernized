INSERT INTO "BSCL.Plataforma.BD::COM.DetalleGrupoActivos" (
	"IdDetalleGrupoActivos",
	"IdGrupoActivos",
	"IdInstrumento",
	"FechaCreacion",
	"FechaIngreso",
	"FechaModificacion",
	"IdEstadoAutorizacion",
	"IdUsuarioCreador",
	"IdEstado",
	"IdEmpresa"
) VALUES (
	:IdDetalleGrupoActivos,
	:IdGrupoActivos,
	:IdInstrumento,
	:FechaCreacion,
	:FechaIngreso,
	:FechaModificacion,
	:IdEstadoAutorizacion,
	:IdUsuarioCreador,
	:IdEstado,
	:IdEmpresa
)
