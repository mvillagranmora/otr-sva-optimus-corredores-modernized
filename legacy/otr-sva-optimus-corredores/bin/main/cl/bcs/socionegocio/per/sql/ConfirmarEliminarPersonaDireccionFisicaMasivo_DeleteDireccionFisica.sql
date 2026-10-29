
UPDATE  DIRF 
SET     DIRF."Borrado" = COALESCE(:Borrado, DIRF."Borrado"),
		DIRF."FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM    "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DIRF
WHERE  DIRF."IdDireccionFisica" IN (:IdEntidadPersonaEliminar);