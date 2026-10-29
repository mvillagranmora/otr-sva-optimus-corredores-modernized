INSERT INTO "BSCL.SocioNegocio.BD::PER.DireccionElectronica" (
	"IdDireccionElectronica",
	"Direccion",
	"CodigoProposito",
	"Comentario",
	"IdEstado",
	"FechaIngreso",
	"FechaCreacion",
	"FechaModificacion",
	"IdPersona",
	"IdEstadoAutorizacion",
	"IdUsuarioCreador"
) VALUES (
	:IdDireccionElectronica,
	:Direccion,
	:CodigoProposito,
	'',
	'ACT',
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	:IdPersona,
	'ING',
	:IdUsuarioCreador
)
