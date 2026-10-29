UPDATE CFP
SET "IdEstado" = 'ANU'
FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP
INNER JOIN #AnularRegistros_ComprobantesAnular AS CA ON (CFP."IdComprobante" = CA."IdComprobante")
WHERE CFP."IdEstado" <> 'ANU';