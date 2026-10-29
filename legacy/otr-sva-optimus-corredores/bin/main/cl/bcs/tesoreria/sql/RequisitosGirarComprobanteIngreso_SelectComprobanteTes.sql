	SELECT 	1 AS "Prioridad",	
		       	CASE 
		       		WHEN COUNT("IdComprobante") < 1 THEN 0 
		       		ELSE 1 
				END AS "Cumple", 
		       	'SIN_ESPECIFICAR_COMPROBANTES_GIRAR' AS "CodigoError",
		       	'' AS "CodigoDato",
		       	'' AS "ValorDato"
		       	,0 AS "IdComprobante"
		FROM #RequisitosGirarComprobanteIngreso_p_ComprobantesAProcesar
	