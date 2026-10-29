SELECT Top 1 COM."IdMoneda" as "p_out_IdMoneda", MAX(MON."Nombre") as "p_out_NombreMoneda", COM."FechaLiquidacion" as "p_out_FechaLiquidacion"
		FROM #RequisitosGirarComprobanteIngreso_p_ComprobantesAProcesar AS GIR	
		INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" AS COM ON (COM."IdComprobante" = GIR."IdComprobante")
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON MON."IdMoneda" = COM."IdMoneda"
		GROUP BY COM."IdMoneda", COM."FechaLiquidacion";
			