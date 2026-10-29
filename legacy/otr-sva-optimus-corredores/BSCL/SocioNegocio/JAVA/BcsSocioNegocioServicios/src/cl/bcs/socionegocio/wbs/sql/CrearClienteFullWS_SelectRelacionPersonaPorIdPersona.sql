SELECT 
	RPER."IdRelacionPersona" AS "IdPersonaRelacionada",
	RPER."IdTipoRelacion" AS "IdTipoRelacion",
	DOC."IdTipoDocumentoIdentidad" AS "TipoDocumentoIdentidad",
	DOC."DocumentoIdentidad" AS	"DocumentoIdentidad",
	PER."Nombres",
	PER."ApellidoPaterno",
	PER."ApellidoMaterno"
FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" AS RPER
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = RPER."IdPersonaDestino")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON (DOC."IdPersona" = RPER."IdPersonaDestino")
WHERE RPER."IdPersonaOrigen" IN (:IdPersona);