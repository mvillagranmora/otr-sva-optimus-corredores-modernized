-- Actualiza el campo Direccion

UPDATE  DOC
SET
	DOC."Direccion" 	= MOD."ValorNuevo",
	DOC."FechaModificacion"   	= CURRENT_UTCTIMESTAMP
	
FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DOC
INNER JOIN "BSCL.SocioNegocio.BD::GBL.ConfirmarModificarPersonaDireccionElectronicaMasivo_SolicitudCambio" MOD ON (DOC."IdDireccionElectronica"= MOD."IdDireccionElectronica")
WHERE 
		MOD."CampoModificado" 		= :CampoModificado      -- Direccion
	AND	MOD."IdTipoEntidad"			= :IdTipoEntidad        -- DIRECCIONELECTRONICA
	AND DOC."IdDireccionElectronica"	= MOD."IdDireccionElectronica";
