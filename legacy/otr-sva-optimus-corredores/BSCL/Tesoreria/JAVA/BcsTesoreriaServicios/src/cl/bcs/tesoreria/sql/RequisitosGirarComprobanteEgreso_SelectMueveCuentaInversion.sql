SELECT  2 AS "Prioridad",
						CASE WHEN (Count(Distinct "MueveCuentaInversion") > 1) THEN 0
							 ELSE  1  
						END AS "Cumple",
						'MUEVE_CUENTA_INVERSION_NO_COINCIDE' AS "CodigoError",
						'IdComprobante' AS "CodigoDato",				
				       	'' AS "ValorDato"		       	
				       	,0 AS "IdComprobante"		       	
				FROM #RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar as GIR
				INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" as COM ON COM."IdComprobante" = GIR."IdComprobante"