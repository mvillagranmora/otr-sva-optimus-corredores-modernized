CREATE LOCAL TEMPORARY TABLE #MOVENTRANSITOAGR (

			"IdCliente" BIGINT,
			"DocumentoIdentidad" VARCHAR(100),
			"IdTipoOperacion" VARCHAR(100),
			"IdClasificacionTipoInstrumento" VARCHAR(100),
			"FechaOperacion" DATE,
			"FechaLiquidacion" DATE,
			"IdCondicionLiquidacion" VARCHAR(100),
			"IdConcepto" VARCHAR(100),
			"IdInstrumento" BIGINT,
			"Nemotecnico" VARCHAR(100),
			"IdMonedaInstrumento"  VARCHAR(10),
			"IdBoveda"  VARCHAR(100),
			"EsNominal"  VARCHAR(100)

)