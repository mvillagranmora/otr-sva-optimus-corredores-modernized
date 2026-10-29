SELECT DDOF."IdTipoOrigenFondo"
, DDOF."IdDeclaracionOrigenFondos"
, DDOF."Valor"
, DDOF."IdUsuarioCreador"
, DDOF."FechaIngreso"
, DDOF."FechaCreacion"
, DDOF."FechaModificacion"
, DDOF."Otros"
FROM "BSCL.SocioNegocio.BD::ENR.DetalleDeclaracionOrigenFondos" DDOF
WHERE DDOF."IdDeclaracionOrigenFondos" IN(:IdsDeclaracionOrigenFondos)