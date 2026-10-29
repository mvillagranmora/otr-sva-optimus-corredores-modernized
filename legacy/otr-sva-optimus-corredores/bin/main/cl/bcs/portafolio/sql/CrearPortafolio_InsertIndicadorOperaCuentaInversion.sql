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
)
SELECT
	"BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL AS "IdIndicador",
	:IdPortafolio AS "IdEntidad",
	TI."IdTipoEntidad",
	TI."IdTipoIndicador",
	Cliente."IdTipoCliente",
	TI."IdEmpresa",
	'S' as "ValorIndicador",
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	'ING' AS "IdEstadoAutorizacion",
	'0001-01-01 00:00:01'
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" P ON Cliente."IdCliente" = P."IdCliente" AND P."IdPortafolio" = :IdPortafolio
INNER JOIN BSCL."BSCL.Plataforma.BD::IND.TipoIndicador" TI
	ON (TI."CodigoTipoIndicador" = 'GEN_OCCINV' AND
		TI."IdTipoEntidad" = 'PORTAFOLIO' AND
		TI."IdUso" = 'PRE' AND
		TI."IdEmpresa" = :IdEmpresa)
WHERE P."IdPortafolio" = :IdPortafolio
AND :PropositoPortafolio = 'PROPOSITOAPV'
;