SELECT
	"IdDocumentoIdentidad",
	"IdTipoDocumentoIdentidad",
	"DocumentoIdentidad"
FROM
	"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
WHERE
	"IdPersona" = :p_IdPersona;