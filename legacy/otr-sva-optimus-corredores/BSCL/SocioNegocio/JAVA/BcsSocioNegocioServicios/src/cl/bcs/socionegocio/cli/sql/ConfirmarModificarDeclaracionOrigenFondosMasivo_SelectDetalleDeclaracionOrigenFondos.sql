SELECT DDOF."IdTipoOrigenFondo"
, DDOF."IdDeclaracionOrigenFondos"
, DDOF."Valor"
, DDOF."Otros"
FROM "BSCL.SocioNegocio.BD::CLI.DetalleDeclaracionOrigenFondos" DDOF
WHERE DDOF."IdDeclaracionOrigenFondos" = :IdDeclaracionOrigenFondos
