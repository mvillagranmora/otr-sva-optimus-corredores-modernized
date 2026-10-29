-- Actualiza los campos del documento de identidad de la Persona
UPDATE  IDENT 
SET     IDENT."IdPersona" 						= COALESCE(:IdPersona,IDENT."IdPersona"),
		IDENT."TipoPersona" 					= COALESCE(:TipoPersona,IDENT."TipoPersona"),
		IDENT."IdEstado" 						= COALESCE(:IdEstado,IDENT."IdEstado"),
		IDENT."NumeroSerie" 					= COALESCE(:NumeroSerie,IDENT."NumeroSerie"),
		IDENT."FechaEmision" 					= COALESCE(:FechaEmision,IDENT."FechaEmision"),
		IDENT."FechaVencimiento" 				= COALESCE(:FechaVencimiento,IDENT."FechaVencimiento"),
		IDENT."Predeterminado" 					= COALESCE(:Predeterminado,IDENT."Predeterminado"),
		IDENT."DocumentoIdentidad" 				= COALESCE(:DocumentoIdentidad,IDENT."DocumentoIdentidad"),
		IDENT."IdTipoDocumentoIdentidad" 		= COALESCE(:IdTipoDocumentoIdentidad,IDENT."IdTipoDocumentoIdentidad"),
		IDENT."DocumentoIdentidadAsociado" 		= COALESCE(:DocumentoIdentidadAsociado,IDENT."DocumentoIdentidadAsociado"),
		IDENT."IdTipoDocumentoIdentidadAsociado" 	= COALESCE(:IdTipoDocumentoIdentidadAsociado,IDENT."IdTipoDocumentoIdentidadAsociado"),
		IDENT."FechaModificacion" 				= CURRENT_UTCTIMESTAMP
FROM	"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" IDENT
WHERE "IdDocumentoIdentidad" 					= :IdDocumentoIdentidad;