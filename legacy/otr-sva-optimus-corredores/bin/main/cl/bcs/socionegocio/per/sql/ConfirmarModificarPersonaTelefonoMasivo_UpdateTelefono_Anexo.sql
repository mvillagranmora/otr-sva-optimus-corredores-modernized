-- Actualiza el campo Anexo

UPDATE  DOC
SET
	DOC."Anexo" 	= MOD."ValorNuevo",
	DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
	
FROM "BSCL.SocioNegocio.BD::PER.Telefono" DOC
INNER JOIN "BSCL.SocioNegocio.BD::GBL.ConfirmarModificarPersonaTelefonoMasivo_SolicitudCambio" MOD ON (DOC."IdTelefono"= MOD."IdTelefono")
WHERE 
		MOD."CampoModificado" 		= :CampoModificado      -- Anexo
	AND	MOD."IdTipoEntidad"			= :IdTipoEntidad        -- TELEFONO
	AND DOC."IdTelefono"			= MOD."IdTelefono";

	
	
	