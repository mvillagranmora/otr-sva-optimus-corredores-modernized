INSERT INTO "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" (
	"IdDocumentoIdentidad",
	"IdPersona",
	"IdTipoDocumentoIdentidad",
	"IdTipoDocumentoIdentidadAsociado",
	"DocumentoIdentidad",
	"IdEstadoAutorizacion",
	"FechaIngreso",
	"FechaCreacion",
	"FechaModificacion",
	"IdUsuarioCreador",
	"Predeterminado",
	"TipoPersona"
) VALUES (
	:IdDocumentoIdentidad,
	:IdPersona,
	:IdTipoDocumentoIdentidad,
	:IdTipoDocumentoIdentidadAsociado,
	:DocumentoIdentidad,
	'ING',
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	:IdUsuarioCreador,
	'S',
	:TipoPersona
)
