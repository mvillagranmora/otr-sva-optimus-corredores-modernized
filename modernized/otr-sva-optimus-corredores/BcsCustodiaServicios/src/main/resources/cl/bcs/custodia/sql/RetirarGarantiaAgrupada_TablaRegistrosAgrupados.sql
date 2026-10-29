CREATE LOCAL TEMPORARY TABLE #REGISTROSAGRUPADOS (

			"p_Cliente" BIGINT,
			"p_Portafolio" BIGINT,
			"p_FechaDeLiquidacion" DATE,
			"p_IdClasificacionIntrumento" VARCHAR(100),
			"p_IdContraparteGarantia" BIGINT,
			"p_IdMonedaInstrumento" VARCHAR(100),
			"p_IdConcepto" VARCHAR(100),
			"p_IdInstrumento" BIGINT,
			"p_IdOrigenGarantia" VARCHAR(100),
			"p_Nemotecnico" VARCHAR(100),
			"CantidadAgrupada" DECIMAL(26,8),
			"CantidadARetirar" DECIMAL(26,8),
			"MontoARetirar" DECIMAL(26,8)

)
