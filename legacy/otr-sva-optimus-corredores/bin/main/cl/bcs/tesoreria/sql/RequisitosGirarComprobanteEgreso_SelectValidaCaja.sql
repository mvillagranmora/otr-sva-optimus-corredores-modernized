SELECT  2 AS "Prioridad",
			CASE WHEN (Count(Distinct COM."IdCaja") > 1) THEN 0  -- caja distinta entre comprobantes
				 ELSE  1  
			END AS "Cumple",
			'CAJA_COMPROBANTES_NO_COINCIDE' AS "CodigoError",						
			'IdComprobante' AS "CodigoDato",				
	       	'' AS "ValorDato"		       	
	       	,0 AS "IdComprobante"
	FROM #RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar as GIR
	INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as COM ON COM."IdComprobante" = GIR."IdComprobante"
	AND COM."IdCaja" <> 0 