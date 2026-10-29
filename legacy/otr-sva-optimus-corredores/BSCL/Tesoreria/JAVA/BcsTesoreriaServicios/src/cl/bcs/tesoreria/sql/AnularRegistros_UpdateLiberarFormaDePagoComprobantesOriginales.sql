UPDATE CFP
SET "IdEstado" 					= CASE WHEN :p_IdModulo != 'MRF' THEN 'ING' ELSE 'ANU' END ,
	"IdUsuarioCertificacion"	= 0,
	"FechaHoraCertificacion"	= '0001-01-01',
	"IdUsuarioLiquidacion"		= 0,
	"FechaHoraLiquidacion"		= '0001-01-01'
FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP
INNER JOIN #AnularRegistros_ComprobantesAnular AS CA ON (CFP."IdComprobante" = CA."IdComprobanteOrigen")
WHERE CFP."IdEstado" <> 'ANU';