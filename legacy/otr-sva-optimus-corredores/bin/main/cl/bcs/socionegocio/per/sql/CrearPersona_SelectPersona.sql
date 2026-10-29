SELECT 
	"p"."IdPersona" AS "IdPersona",
	"tp"."Descripcion" AS "TipoPersona",
	"p"."Nombres" || ' ' || "p"."ApellidoPaterno" || ' ' || "p"."ApellidoMaterno" AS "NombreCompleto",
	"di"."DocumentoIdentidad" AS "DocumentoIdentidad",
	"tdi"."NombreTipoDocumentoIdentidad" AS "TipoDocumentoIdentidad",
	"pais"."NombrePais" AS "PaisNacimiento",
	"ec"."Nombre" AS "EstadoCivil",
	"g"."Descripcion" AS "Genero",
	"paiRes"."NombrePais" AS "PaisResidencia",
	"rc"."Nombre" AS "RegimenConyugal"
		FROM "BSCL.SocioNegocio.BD::PER.Persona" "p"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.TipoPersona" "tp"
			ON ("p"."Tipo" = "tp"."IdTipoPersona")
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" "di"
			ON("p"."IdDocumentoIdentidad" = "di"."IdDocumentoIdentidad")
		LEFT JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" "tdi"
			ON("di"."IdTipoDocumentoIdentidad" = "tdi"."IdTipoDocumentoIdentidad")
		LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" "pais"
			ON("p"."IdPaisNacimiento" = "pais"."IdPais")
		LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" "paiRes"
			ON("p"."IdPaisResidencia" = "paiRes"."IdPais") 
		LEFT JOIN "BSCL.Plataforma.BD::REG.TipoEstadoCivil" "ec"
			ON("p"."EstadoCivil" = "ec"."IdTipoEstadoCivil")
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.TipoGenero" "g"
			ON("p"."Sexo" = "g"."IdTipoGenero")
		LEFT JOIN "BSCL.Plataforma.BD::REG.TipoRegimenConyugal" "rc"
			ON("rc"."IdTipoRegimenConyugal" = "p"."RegimenConyugal")
				WHERE "p"."IdPersona" IN(:IdPersona)