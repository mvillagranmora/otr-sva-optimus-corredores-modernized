SELECT  1 AS "Prioridad",
				CASE WHEN (COALESCE(COM."IdComprobante",0) = 0) or GIR."IdComprobante" = 0 THEN 0 ELSE 1 END AS "Cumple",
				'COMPROBANTE_TESORERIA_INVALIDO' AS "CodigoError",
				'IdComprobante' AS "CodigoDato",
		       	CAST(GIR."IdComprobante" AS NVARCHAR(50)) AS "ValorDato"
		       	,GIR."IdComprobante" AS "IdComprobante"
		FROM #RequisitosGirarComprobanteIngreso_p_ComprobantesAProcesar GIR		
		LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON COM."IdComprobante" = GIR."IdComprobante"		