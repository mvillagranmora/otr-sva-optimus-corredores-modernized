SELECT  2 AS "Prioridad",
			CASE WHEN (Count(Distinct "IdTipoCanalIngreso") > 1) THEN 0  -- fecha de liquidación distinta entre comprobantes
				 ELSE  1  
			END AS "Cumple",
			'ORIGEN_COMPROBANTES_NO_COINCIDE' AS "CodigoError",
			'IdComprobante' AS "CodigoDato",				
	       	'' AS "ValorDato"		       	
	       	,0 AS "IdComprobante"		       	
	FROM #RequisitosGirarComprobanteIngreso_p_ComprobantesAProcesar as GIR
	INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" as COM ON COM."IdComprobante" = GIR."IdComprobante"