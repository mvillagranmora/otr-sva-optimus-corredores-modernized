SELECT DDOF."IdTipoOrigenFondo"
, DDOF."IdDeclaracionOrigenFondos"
, DDOF."Valor"
, DDOF."IdUsuarioCreador"
, DDOF."FechaIngreso"
, DDOF."FechaCreacion"
, DDOF."FechaModificacion"
, DDOF."IdEstado"
, DDOF."Otros"
FROM "BSCL.SocioNegocio.BD::CLI.DetalleDeclaracionOrigenFondos" DDOF
WHERE DDOF."IdDeclaracionOrigenFondos" IN(:IdsDeclaracionOrigenFondos)