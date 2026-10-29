SELECT  2 AS "Prioridad",
						CASE WHEN (COM."TipoComprobante" = 'PAREGR') THEN 0  -- comprobante es partida egreso
							 ELSE  1  
						END AS "Cumple",
						CASE WHEN (COM."TipoComprobante" = 'PAREGR') THEN 'COMPROBANTE_TESORERIA_ES_PAREGR'
							 ELSE  ''
						END AS "CodigoError",				
						'IdComprobante' AS "CodigoDato",								
				       	CAST(COM."IdComprobante" as NVARCHAR(50)) AS "ValorDato"		       	
				       	,COM."IdComprobante"		       	
				FROM #RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar AS GIR
				INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as COM ON COM."IdComprobante" = GIR."IdComprobante"