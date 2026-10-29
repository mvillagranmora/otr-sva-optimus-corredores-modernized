
UPDATE  TARB 
SET     TARB."Borrado" = COALESCE(:Borrado, TARB."Borrado"),
		TARB."FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM    "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" AS TARB
WHERE  TARB."IdTarjetaBancaria" IN (:IdEntidadPersonaEliminar);