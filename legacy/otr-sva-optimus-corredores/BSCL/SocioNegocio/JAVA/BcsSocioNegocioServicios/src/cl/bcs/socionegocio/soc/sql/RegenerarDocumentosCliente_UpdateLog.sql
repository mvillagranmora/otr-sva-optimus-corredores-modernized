UPDATE "BSCL.SocioNegocio.BD::CLI.LogGenerarContratos" AS ORI
SET ORI."Estado" =ACT."Estado",
	ORI."IdPrograma"=ACT."IdPrograma"
FROM "BSCL.SocioNegocio.BD::CLI.LogGenerarContratos" AS ORI
INNER JOIN #TablaLogActulizar AS ACT ON ACT."IdLog" = ORI."Idlog"