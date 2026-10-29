-- Actualiza el campo IdTipoIdentificadorCuenta

UPDATE  DOC
SET
	DOC."IdTipoIdentificadorCuenta" 	= MOD."ValorNuevo",
	DOC."FechaModificacion"   	= CURRENT_UTCTIMESTAMP
	
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" DOC
INNER JOIN "BSCL.SocioNegocio.BD::GBL.ConfirmarModificarPersonaCuentaBancariaMasivo_SolicitudCambio" MOD ON (DOC."IdCuentaBancaria"= MOD."IdCuentaBancaria")
WHERE 
		MOD."CampoModificado" 		= :CampoModificado      -- IdTipoIdentificadorCuenta
	AND	MOD."IdTipoEntidad"			= :IdTipoEntidad        -- CUENTABANCARIA
	AND DOC."IdCuentaBancaria"		= MOD."IdCuentaBancaria";
