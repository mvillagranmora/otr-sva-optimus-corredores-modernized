INSERT INTO "BSCL.Plataforma.BD::IND.Indicador"(
	"IdIndicador",
	"IdEntidad",
	"IdTipoEntidad",
	"IdTipoIndicador",
	"ModeloCliente",
	"IdEmpresa",
	"ValorIndicador",
	"FechaIngreso",
	"FechaCreacion",
	"FechaModificacion",
	"IdEstadoAutorizacion",
	"FechaVencimiento"
)VALUES(
	:IdIndicador,
	:IdEntidad,
	:IdTipoEntidad,
	:IdTipoIndicador,
	:ModeloCliente,
	:IdEmpresa,
	:ValorIndicador,
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	:IdEstadoAutorizacion,
	'0001-01-01 00:00:01'
);