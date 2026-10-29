-- Actualiza el campo IdTipoTelefono

UPDATE  DOC
SET
	DOC."IdTipoTelefono" 	= MOD."ValorNuevo",
	DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
	
FROM "BSCL.SocioNegocio.BD::PER.Telefono" DOC
INNER JOIN "BSCL.SocioNegocio.BD::GBL.ConfirmarModificarPersonaTelefonoMasivo_SolicitudCambio" MOD ON (DOC."IdTelefono"= MOD."IdTelefono")
WHERE 
		MOD."CampoModificado" 		= :CampoModificado      -- IdTipoTelefono
	AND	MOD."IdTipoEntidad"			= :IdTipoEntidad        -- TELEFONO
	AND DOC."IdTelefono"			= MOD."IdTelefono";
