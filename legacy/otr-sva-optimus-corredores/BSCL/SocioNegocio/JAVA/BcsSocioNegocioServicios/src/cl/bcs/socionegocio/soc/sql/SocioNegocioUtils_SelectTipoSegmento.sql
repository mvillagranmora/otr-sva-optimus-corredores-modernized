SELECT
	TipoSegmento."IdSegmentoCliente"			AS "IdSegmentoCliente"
	, TipoSegmento."NombreSegmentoCliente"		AS "NombreSegmentoCliente"
	, TipoSegmento."DescripcionSegmento"		AS "DescripcionSegmentoCliente"
FROM "BSCL.SocioNegocio.BD::CLI.TipoSegmento" TipoSegmento
WHERE TipoSegmento."IdEstado" = 'ACT'
	AND TipoSegmento."Borrado" = 'N'
	AND TipoSegmento."IdEmpresa" = :IdEmpresa;