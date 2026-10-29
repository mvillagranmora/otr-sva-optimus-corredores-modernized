UPDATE COM
SET "IdEstado" = CASE WHEN :p_IdModulo != 'MRF' THEN 'ING' ELSE 'ANU' END  
FROM "BSCL.Tesoreria.BD::TES.Comprobante" AS COM
INNER JOIN #AnularRegistros_ComprobantesAnular AS CA ON (COM."IdComprobante" = CA."IdComprobanteOrigen");