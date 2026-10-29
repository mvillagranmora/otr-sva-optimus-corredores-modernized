SELECT 
	"c"."IdCliente" AS "IdCliente",
	"p"."Nombres" AS "Nombres",
	"p"."ApellidoPaterno" AS "ApellidoPaterno",
	"p"."ApellidoMaterno" AS "ApellidoMatero",
	"di"."DocumentoIdentidad" AS "DocumentoIdentidad",
	"di"."IdTipoDocumentoIdentidad" AS "IdTipoDocumentoIdentidad",
	"c"."IdEstado" AS "EstadoCliente",
	"c"."IdPortafolioDefecto" AS "PortafolioDefecto",
	"por"."IdEstado" AS "EstadoPortafolio" 
		FROM "BSCL.SocioNegocio.BD::PER.Persona" "p"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" "di"
			ON ("p"."IdDocumentoIdentidad" = "di"."IdDocumentoIdentidad")
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" "c"
			ON ("p"."IdPersona" = "c"."IdPersona")
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" "por"
			ON ("c"."IdPortafolioDefecto" = "por"."IdPortafolio")
			WHERE "c"."IdCliente" IN (:IdCliente)