SELECT 
	"IdSegmentoCliente",
	"NombreSegmentoCliente",
	"DescripcionSegmento"
		FROM "BSCL.SocioNegocio.BD::CLI.TipoSegmento" 
		WHERE "IdSegmentoCliente" IN (:IdSegmento)