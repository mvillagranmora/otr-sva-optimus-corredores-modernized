SELECT  2 AS "Prioridad",
						CASE WHEN COALESCE(COM."IdComprobante",0) = 0 THEN 0  -- Si el comprobante no existe
						 	 WHEN (COM."IdEstado" = 'ING') THEN 0
							 WHEN (COM."IdEstado" = 'BLO') THEN 0
							 WHEN (COM."IdEstado" = 'GIR') THEN 0
							 WHEN (COM."IdEstado" = 'ANU') THEN 0
							 ELSE  1  -- si comprobante está liquidado(LIQ) ó GIP(girado parcialmente)
						END AS "Cumple",
						
						CASE WHEN COALESCE(COM."IdComprobante",0) = 0 THEN 'COMPROBANTE_TESORERIA_NO_EXISTE'
						 	 WHEN (COM."IdEstado" = 'ING') THEN 'COMPROBANTE_TESORERIA_NO_LIQUIDADO'
							 WHEN (COM."IdEstado" = 'BLO') THEN 'COMPROBANTE_TESORERIA_NO_LIQUIDADO'
							 WHEN (COM."IdEstado" = 'GIR') THEN 'COMPROBANTE_TESORERIA_NO_LIQUIDADO'
							 WHEN (COM."IdEstado" = 'ANU') THEN 'COMPROBANTE_TESORERIA_NO_LIQUIDADO'
							 ELSE  ''
						END AS "CodigoError",
						
						'IdComprobante' AS "CodigoDato",								
				       	CAST(GIR."IdComprobante" as NVARCHAR(50)) AS "ValorDato"		       	
				       	,GIR."IdComprobante"
				FROM #RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar as GIR
				LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as COM ON COM."IdComprobante" = GIR."IdComprobante"