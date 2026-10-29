-- Actualiza el campo IdPais

UPDATE  DOC
SET
	DOC."IdPais" 	= MOD."ValorNuevo",
	DOC."FechaModificacion"   	= CURRENT_UTCTIMESTAMP
	
FROM "BSCL.SocioNegocio.BD::PER.Telefono" DOC
INNER JOIN "BSCL.SocioNegocio.BD::GBL.ConfirmarModificarPersonaTelefonoMasivo_SolicitudCambio" MOD ON (DOC."IdTelefono"= MOD."IdTelefono")
WHERE 
		MOD."CampoModificado" 		= :CampoModificado      -- IdPais
	AND	MOD."IdTipoEntidad"			= :IdTipoEntidad        -- TELEFONO
	AND DOC."IdTelefono"			= MOD."IdTelefono";

	