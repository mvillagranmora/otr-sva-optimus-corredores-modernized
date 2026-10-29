SELECT  2 AS "Prioridad",
						CASE WHEN (Count(Distinct "IdMoneda") > 1) THEN 0  -- moneda distinta entre comprobantes
							 ELSE  1  
						END AS "Cumple",
						'MONEDA_COMPROBANTES_NO_COINCIDE' AS "CodigoError",
						'IdComprobante' AS "CodigoDato",				
				       	'' AS "ValorDato"		       	
				       	,0 AS "IdComprobante"		       	
				FROM #RequisitosGirarComprobanteIngreso_p_ComprobantesAProcesar as GIR
				INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as COM ON COM."IdComprobante" = GIR."IdComprobante"	 