UPDATE CD
SET "IdEstado" = 'ANU'
FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS CD
INNER JOIN #AnularRegistros_ComprobantesAnular AS CA ON (CD."IdComprobante" = CA."IdComprobante")
WHERE CD."IdEstado" <> 'ANU';
