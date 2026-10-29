
UPDATE  IDENT 
SET     IDENT."Borrado" = COALESCE(:Borrado, IDENT."Borrado"),
		IDENT."FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM    "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT
WHERE  IDENT."IdDocumentoIdentidad" IN (:IdEntidadPersonaEliminar);
