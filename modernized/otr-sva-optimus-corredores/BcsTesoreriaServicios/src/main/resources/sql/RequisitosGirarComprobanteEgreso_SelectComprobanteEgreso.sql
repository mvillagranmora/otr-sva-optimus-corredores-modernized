				SELECT  2 AS "Prioridad",
						CASE WHEN (COM."IdTipoMovimiento" = 'MOVING') THEN 0  -- comprobante es ingreso
							 ELSE  1  -- comprobante es egreso(MOVEGR)
						END AS "Cumple",
						CASE WHEN (COM."IdTipoMovimiento" = 'MOVING') THEN 'COMPROBANTE_TESORERIA_ES_INGRESO'
							 ELSE  ''
						END AS "CodigoError",				
						'IdComprobante' AS "CodigoDato",								
				       	CAST(COM."IdComprobante" as NVARCHAR(50)) AS "ValorDato"		       	
				       	,COM."IdComprobante"		       	
				FROM #RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar as GIR
				INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as COM ON COM."IdComprobante" = GIR."IdComprobante"