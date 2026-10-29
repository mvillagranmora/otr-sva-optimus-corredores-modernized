SELECT Top 1 COM."IdCaja" as "p_out_IdCaja", MAX(COM."NombreCaja") as "p_out_NombreCaja"
		FROM #RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar AS GIR						
		INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" AS COM ON COM."IdComprobante" = GIR."IdComprobante"													
		WHERE COM."IdCaja" <> 0  -- Considerar comprobantes con caja asignada
		GROUP BY COM."IdCaja";