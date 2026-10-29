INSERT INTO "BSCL.Plataforma.BD::IND.Indicador" (
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
)
SELECT
	"BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL AS "IdIndicador",
	:IdEntidad AS "IdEntidad",
	TI."IdTipoEntidad",
	TI."IdTipoIndicador",
	CLI."IdTipoCliente" AS "ModeloCliente",
	TI."IdEmpresa",
	DET."IdDetalleIndicador" AS "ValorIndicador",
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	'ING' AS "IdEstadoAutorizacion",
	'0001-01-01 00:00:01'
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORT ON CLI."IdCliente" = PORT."IdCliente" AND PORT."IdPortafolio" = :IdEntidad
INNER JOIN  "BSCL.Plataforma.BD::IND.TipoIndicador" TI ON TI."CodigoTipoIndicador" = 'COBROCUST' AND TI."IdTipoEntidad" = 'PORTAFOLIO'
INNER JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" DET ON UPPER(DET."DetalleIndicador") = UPPER(:ValorIndicador) AND TI."IdTipoIndicador" = DET."IdTipoIndicador"
WHERE PORT."IdPortafolio" = :IdEntidad;