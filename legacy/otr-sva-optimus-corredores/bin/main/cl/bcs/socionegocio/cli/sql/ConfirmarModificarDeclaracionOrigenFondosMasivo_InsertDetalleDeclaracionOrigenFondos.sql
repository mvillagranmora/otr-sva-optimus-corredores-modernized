INSERT INTO "BSCL.SocioNegocio.BD::CLI.DetalleDeclaracionOrigenFondos" (
	"IdTipoOrigenFondo",
	"IdDeclaracionOrigenFondos",
	"Valor",
	"IdUsuarioCreador",
	"FechaIngreso",
	"FechaCreacion",
	"FechaModificacion", 
	"IdEstado",
	"Otros")
VALUES (
	:IdTipoOrigenFondo,
	:IdDeclaracionOrigenFondos,
	:Valor,
	:IdUsuarioCreador,
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	'ACT',
	COALESCE(:Otros, '')
);
