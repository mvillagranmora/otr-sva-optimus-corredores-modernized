SELECT
	"p"."IdPersona" AS "IdPersona",
	"p"."Tipo" AS "Tipo",
	"p"."Nombres" AS "Nombres",
	"p"."ApellidoPaterno" AS "ApellidoPaterno",
	"p"."ApellidoMaterno" AS "ApellidoMaterno",
	"di"."DocumentoIdentidad" AS "DocumentoIdentidad",
	"di"."IdTipoDocumentoIdentidad" AS "IdTipoDocumentoIdentidad",
	"p"."RazonSocial" AS "RazonSocial",
	"p"."FechaNacimiento" AS "FechaNacimiento",
	"p"."Sexo" AS "Sexo",
	"p"."RegimenConyugal" AS "RegimenConyugal",
	"p"."EstadoCivil" AS "EstadoCivil",
	"p"."IdProfesion" AS "IdProfesion", 
	"p"."IdPersonaConyuge" AS "IdPersonaConyuge",
	"p"."IdPaisNacimiento" AS "IdPaisNacimiento",
	"p"."IdPaisNacionalidadPrincipal" AS "IdPaisNacionalidadPrincipal",
	"c"."IdCliente" AS "IdCliente",
	"c"."IdEstado" AS "IdEstado",
	"p"."IdPaisResidencia" AS "IdPaisResidencia",
	"p"."IdSituacionLaboral" AS "IdSituacionLaboral"
	FROM "BSCL.SocioNegocio.BD::PER.Persona" "p"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" "di"
		ON ("p"."IdDocumentoIdentidad" = "di"."IdDocumentoIdentidad")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" "c"
		ON ("p"."IdPersona" = "c"."IdPersona")
			WHERE "di"."DocumentoIdentidad" IN(:DocumentoIdentidad) 
					OR "p"."IdPersona" IN(:IdPersona)
			