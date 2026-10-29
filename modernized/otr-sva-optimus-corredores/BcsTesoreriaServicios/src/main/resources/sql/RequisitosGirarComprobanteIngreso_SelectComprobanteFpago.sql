SELECT  2 AS "Prioridad",
		CASE WHEN COALESCE(CFP."IdComprobanteFormaDePago",0) = 0 THEN 0
			 ELSE  1  
		END AS "Cumple",
		'COMPROBANTES_SIN_FORMA_DE_PAGO_ASIGNADA' AS "CodigoError",	
		'IdComprobante' AS "CodigoDato",
       	CAST(COM."IdComprobante" as NVARCHAR(50)) AS "ValorDato"		       	
       	,COM."IdComprobante"				       	
FROM #RequisitosGirarComprobanteIngreso_p_ComprobantesAProcesar as GIR
INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as COM ON COM."IdComprobante" = GIR."IdComprobante"
LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP ON (CFP."IdComprobante" = COM."IdComprobante" AND (CFP."IdEstado" <> 'ANU' AND CFP."IdEstado" <> 'LIQ' AND CFP."IdEstado" <> 'GIR'))