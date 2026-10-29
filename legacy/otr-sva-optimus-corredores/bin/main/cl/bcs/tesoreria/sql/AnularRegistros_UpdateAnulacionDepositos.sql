UPDATE DD
SET "IdEstado" = 'ANU'
FROM "BSCL.Tesoreria.BD::TES.DepositoDetalle" AS DD
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP ON (CFP."IdComprobanteFormaDePago" = DD."IdComprobanteFormaDePago")		
INNER JOIN #AnularRegistros_ComprobantesAnular AS CA ON (CFP."IdComprobante" = CA."IdComprobante");