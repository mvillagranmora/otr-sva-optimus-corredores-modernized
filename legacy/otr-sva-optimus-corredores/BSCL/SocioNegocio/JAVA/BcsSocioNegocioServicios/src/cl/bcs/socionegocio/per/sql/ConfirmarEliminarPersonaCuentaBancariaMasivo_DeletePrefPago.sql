
UPDATE  PPP 
SET     PPP."IdEstado" = 'INA',
		PPP."FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM    "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" AS PPP
WHERE  PPP."IdCuentaBancaria" IN (:IdEntidadPersonaEliminar);