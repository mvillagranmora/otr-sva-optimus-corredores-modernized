UPDATE COM
SET 
	"IdEstado" 				= 'ANU', 
	"IdUsuarioAnulacion" 	= :p_IdUsuario,
	"FechaHoraAnulacion"	= CURRENT_UTCTIMESTAMP			
FROM "BSCL.Tesoreria.BD::TES.Comprobante" AS COM
WHERE COM."IdComprobante" IN (SELECT distinct "IdComprobante" FROM #AnularRegistros_ComprobantesAnular);