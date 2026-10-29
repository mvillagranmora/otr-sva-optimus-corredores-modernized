SELECT distinct 
	DOC."IdPersona" 
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC
WHERE DOC."DocumentoIdentidad" IN (:DocumentosIdentidad);