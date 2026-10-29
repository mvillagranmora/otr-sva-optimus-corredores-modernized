SELECT
	NTD."IdNominaTransferenciaDetalle" AS "Id",
	'TES.NominaTransferenciaDetalle'   AS "Entidad"
FROM #AnularRegistros_ComprobantesAnular AS CA
INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON (COM."IdComprobante" = CA."IdComprobante")
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP ON (COM."IdComprobante" = CFP."IdComprobante")
INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" AS NTD ON (CFP."IdComprobanteFormaDePago" = NTD."IdComprobanteFormaDePago")
AND NTD."IdEstado" <> 'ANU';