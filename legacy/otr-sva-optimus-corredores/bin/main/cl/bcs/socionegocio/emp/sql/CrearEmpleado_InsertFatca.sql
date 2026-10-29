INSERT INTO "BSCL.SocioNegocio.BD::PER.Fatca" (
	"IdPersona",
	"IdEstado",
	"IdClasificacionFatca",
	"IdClasificacionEmpresaFatca",
	"NumeroGIIN",
	"AutorizacionInformarOrganismo",
	"FechaIngreso",
	"FechaCreacion",
	"FechaModificacion",
	"IdUsuarioCreador",
	"IdUsuarioModificador"
) VALUES (
	:IdPersona,
	'ACT',
	'NOUSENTITY',
	:IdClasificacionEmpresaFatca,
	'',
	'N',
	:FechaIngreso,
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	:IdUsuarioCreador,
	:IdUsuarioCreador
)
