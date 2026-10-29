
UPDATE  TEL 
SET     TEL."Borrado" = COALESCE(:Borrado, TEL."Borrado"),
		TEL."FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM    "BSCL.SocioNegocio.BD::PER.Telefono" AS TEL
WHERE  TEL."IdTelefono" IN (:IdEntidadPersonaEliminar);