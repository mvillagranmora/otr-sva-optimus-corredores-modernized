SELECT  2 AS "Prioridad",
						CASE WHEN (CFP."IdComprobantePartida" IS NOT NULL AND CFP."IdComprobantePartida" != 0) THEN 0  -- La forma de pago ya tiene asociada PAREGR.
							 ELSE  1  -- No se le ha asignado PAREGR.
						END AS "Cumple",
						'FORMAPAGO_ASIGNADA_PAREGR' AS "CodigoError",
						'IdComprobanteFormaDePago' AS "CodigoDato",								
				       	CAST(CFP."IdComprobanteFormaDePago" as NVARCHAR(50)) AS "ValorDato"		       	
				       	,CFP."IdComprobanteFormaDePago"
				FROM #RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar as GIR
				INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as COM ON COM."IdComprobante" = GIR."IdComprobante"
				LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP ON (CFP."IdComprobante" = COM."IdComprobante" AND (CFP."IdEstado" <> 'ANU' AND CFP."IdEstado" <> 'GIR' AND CFP."IdEstado" <> 'MOD'))