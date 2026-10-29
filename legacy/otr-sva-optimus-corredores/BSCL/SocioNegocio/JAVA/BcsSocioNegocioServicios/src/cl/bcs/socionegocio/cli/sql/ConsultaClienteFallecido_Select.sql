SELECT CLI."IdPersona",
	   CLI."NombreCompleto" AS "NombreCompleto",
	   CLI."DocumentoIdentidad" AS "DocumentoIdentidad",
	   CASE WHEN CLI."FechaDefuncion" IS NOT NULL AND CLI."FechaDefuncion" != '' THEN TO_DATE(cli."FechaDefuncion") ELSE TO_DATE('0001-01-01') END AS "FechaDefuncion",
	   CASE WHEN CLI."NombreEstado" != 'BLOQUEADO' AND PER."NombreEstado" = 'BLOQUEADO' THEN PER."NombreEstado" ELSE CLI."NombreEstado" END AS "NombreEstado",
	   CASE WHEN (CLI."NombreTipoBloqueo" != 'BLOQUEO POR MUERTE' OR CLI."NombreTipoBloqueo" IS NULL) AND PER."NombreTipoBloqueo" = 'BLOQUEO POR MUERTE' THEN PER."NombreTipoBloqueo" ELSE CLI."NombreTipoBloqueo" END AS "NombreTipoBloqueo",
	   CASE WHEN CLI."FechaBloqueo" IS NOT NULL AND CLI."FechaBloqueo" != '' THEN cli."FechaBloqueo" ELSE '0001-01-01' END AS "FechaBloqueo",
	   CASE WHEN CLI."ComentarioBloqueo" = '' AND PER."ComentarioBloqueo" != '' THEN PER."ComentarioBloqueo" ELSE CLI."ComentarioBloqueo" END AS "ComentarioBloqueo",
	   PER."IdPersona" AS "IdPersona"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON CLI."IdPersona" = PER."IdPersona"
WHERE "IdEmpresa"=:IdEmpresa AND (CLI."NombreTipoBloqueo"='BLOQUEO POR MUERTE' OR PER."NombreTipoBloqueo"='BLOQUEO POR MUERTE');