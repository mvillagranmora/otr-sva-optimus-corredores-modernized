
UPDATE  DIRE 
SET     DIRE."Borrado" = COALESCE(:Borrado, DIRE."Borrado"),
		DIRE."FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM    "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DIRE
WHERE  DIRE."IdDireccionElectronica" IN (:IdEntidadPersonaEliminar);