
UPDATE  CTAB 
SET     CTAB."Borrado" = COALESCE(:Borrado, CTAB."Borrado"),
		CTAB."FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM    "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CTAB
WHERE  CTAB."IdCuentaBancaria" IN (:IdEntidadPersonaEliminar);