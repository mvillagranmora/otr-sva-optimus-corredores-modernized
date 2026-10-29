SELECT 
	"c"."IdCliente" AS "IdCliente",
	"p"."Nombres" AS "Nombres",
	"p"."ApellidoPaterno" AS "ApellidoPaterno",
	"p"."ApellidoMaterno" AS "ApellidoMaterno",
	"di"."DocumentoIdentidad" AS "DocumentoIdentidad",
	"di"."IdTipoDocumentoIdentidad" AS "IdTipoDocumentoIdentidad",
	"c"."IdEstado" AS "IdEstado" 
		FROM "BSCL.SocioNegocio.BD::PER.Persona" "p"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" "di"
			ON ("p"."IdDocumentoIdentidad" = "di"."IdDocumentoIdentidad")
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" "c"
			ON ("p"."IdPersona" = "c"."IdPersona")
				WHERE "c"."IdCliente" IN (:IdCliente)