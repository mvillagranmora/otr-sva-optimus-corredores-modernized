INSERT INTO "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" (
	"IdDocumentoIdentidad",
	"IdPersona",
	"IdTipoDocumentoIdentidad",
	"DocumentoIdentidad",
	"IdEstadoAutorizacion",
	"FechaIngreso",
	"FechaCreacion",
	"FechaModificacion",
	"IdUsuarioCreador",
	"TipoPersona"
) VALUES (
	:IdDocumentoIdentidad,
	:IdPersona,
	:IdTipoDocumentoIdentidad,
	:DocumentoIdentidad,
	'ING',
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	:IdUsuarioCreador,
	:TipoPersona
)
