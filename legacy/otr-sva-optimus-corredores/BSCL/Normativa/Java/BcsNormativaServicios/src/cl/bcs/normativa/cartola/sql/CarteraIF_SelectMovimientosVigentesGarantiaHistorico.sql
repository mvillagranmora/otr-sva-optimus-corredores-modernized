WITH 
	Garantia AS (
		SELECT 	 CAR."IdCliente"
				,CAR."IdPortafolio"
				,CAR."FolioCartera"
				,GAR."INGFechaOperacion" AS "FechaOperacion"
				,GAR."INGFechaLiquidacion" as "FechaLiquidacion"
				,CAR."IdBoveda"
				,GAR."IdTipoInstrumento"
				,GAR."IdInstrumento"
				,GAR."IdEmisor"
				,GAR."FechaVencimientoInstrumento"
				,GAR."IdTipoReajuste"
				,GAR."FechaEmisionInstrumento"
				,GAR."Nemotecnico"
				,CAR."INGCantidad" AS "CantidadOriginal"
				,CAR."INGValorRescate" AS "ValorRescateOriginal"
				,GAR."INGCantidad" AS "CantidadGarantia"
				,GAR."INGValorRescate" AS "ValorRescateGarantia"
				,GAR."IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,CAR."INGTasaCompra" AS "TasaCompra"
				,CAR."INGPrecioCompra" AS "PrecioCompra"
				,GAR."INGMontoATasaCompraActual" AS "MontoCompraMonedaSistema"
				,GAR."INGMontoATasaCompraActual" AS "MontoCompraMonedaAdicional"
				,GAR."INGTasaMercadoActual" AS "TasaMercado"
				,GAR."INGPrecioMercadoActual" as "PrecioMercado"
				,GAR."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaSistema"
				,GAR."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,GAR."INGMontoInteresCorridoMercadoActual" AS "MontoInteresCorridoMercadoActualMonedaSistema"
				,GAR."INGMontoInteresCorridoMercadoActualMonedaAdicional" AS "MontoInteresCorridoMercadoActualMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,GAR."TieneCorteDeCupon"
				,GAR."INGFechaPagoCorteCupon" AS "FechaPagoCorteCupon"
				,CASE WHEN GAR."INGFechaPagoCorteCupon" <= :FechaHasta THEN GAR."INGMontoCapitalCorteCupon" ELSE TO_DECIMAL(0,26,8) END as "CuponesPagadosMonedaSistema"
		FROM "BSCL.Custodia.BD::HIS.Garantia" AS GAR
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON GAR."IdPortafolio" = P."IdPortafolio"
		LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" CAR ON CAR."IdCartera" = GAR."IdCarteraOrigen" AND CAR."FechaRespaldo" = GAR."FechaRespaldo"
		LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" CAR_VIG ON CAR_VIG."FolioCartera" = GAR."FolioCarteraOrigen" AND CAR_VIG."FechaRespaldo" = GAR."FechaRespaldo" AND CAR_VIG."IdEstado" = 'VIG'
		WHERE  GAR."FechaRespaldo" = :FechaHasta
		  AND GAR."IdClasificacionTipoInstrumento" = 'IF'
		  AND GAR."IdEstado" = 'VIG'
		  AND GAR."IdEmpresa" = :IdEmpresa
		  AND GAR."INGFechaOperacion" <= :FechaHasta
		  AND CAR_VIG."IdCartera" IS NULL
	)
	SELECT   "IdCliente","IdPortafolio","FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento"
			,"IdInstrumento","IdEmisor","FechaVencimientoInstrumento","IdTipoReajuste","FechaEmisionInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","ValorRescateOriginal","TasaCompra","PrecioCompra"
			,"MontoCompraMonedaSistema","MontoCompraMonedaAdicional","TasaMercado","PrecioMercado","TieneCorteDeCupon","FechaPagoCorteCupon"
			,IFNULL(SUM("CantidadGarantia"),TO_DECIMAL(0,26,8)) AS "CantidadGarantia"
			,IFNULL(SUM("ValorRescateGarantia"),TO_DECIMAL(0,26,8)) AS "ValorRescateGarantia"
			,IFNULL(SUM("MontoMercadoMonedaSistema"),TO_DECIMAL(0,26,8)) AS "MontoMercadoMonedaSistema"
			,IFNULL(SUM("MontoMercadoMonedaAdicional"),TO_DECIMAL(0,26,8)) AS "MontoMercadoMonedaAdicional"
			,IFNULL(SUM("MontoInteresCorridoMercadoActualMonedaSistema"),TO_DECIMAL(0,26,8)) AS "MontoInteresCorridoMercadoActualMonedaSistema"
			,IFNULL(SUM("MontoInteresCorridoMercadoActualMonedaAdicional"),TO_DECIMAL(0,26,8)) AS "MontoInteresCorridoMercadoActualMonedaAdicional"			
			,IFNULL(SUM("CuponesPagadosMonedaSistema"),TO_DECIMAL(0,26,8)) AS "CuponesPagadosMonedaSistema"
	FROM Garantia
	GROUP BY "IdCliente","IdPortafolio","FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento","IdInstrumento"
			,"IdEmisor","FechaVencimientoInstrumento","IdTipoReajuste","FechaEmisionInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","ValorRescateOriginal","TasaCompra","PrecioCompra"
			,"MontoCompraMonedaSistema","MontoCompraMonedaAdicional","TasaMercado","PrecioMercado","TieneCorteDeCupon","FechaPagoCorteCupon";