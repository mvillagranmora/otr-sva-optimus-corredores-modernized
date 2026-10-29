UPDATE  DOC
SET
	DOC."PorDefecto" = 'N',
	DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" DOC, "BSCL.Plataforma.BD::PAG.PreferenciaPago" PP
WHERE DOC."IdPreferenciaPago" 	= PP."IdPreferenciaPago" 
AND DOC."IdPortafolio" 			= :IdPortafolio
AND PP."IdEmpresa" 				= DOC."IdEmpresa" 
AND PP."IdEmpresa" 				= :IdEmpresa
AND PP."IdTipoOperacion" 		= :IdTipoOperacion
AND PP."IdMoneda" 				= :IdMoneda
AND DOC."IdPreferenciaPagoPortafolio" != :IdPreferenciaPagoPortafolio;