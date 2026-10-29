UPDATE "BSCL.SocioNegocio.BD::PER.Persona"
SET 
	"IdDocumentoIdentidadAdicional" = :p_IdDocumentoIdentidadAdicional
WHERE "IdPersona" = :p_IdPersona;