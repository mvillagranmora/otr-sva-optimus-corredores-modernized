WITH 
	GarantiaPaso AS (	
		SELECT "IdGarantiaHistorico" , "IdCarteraOrigen" 
		FROM "BSCL.Custodia.BD::HIS.Garantia" G
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON G."IdPortafolio" = P."IdPortafolio"
		WHERE G."FechaRespaldo" IN (:FechaHasta) 
	),
	Garantia AS (
		SELECT 	 CAR."IdCliente"
				,CAR."IdPortafolio"
				,CAR."FolioCartera"
				,GAR."INGFechaOperacion" AS "FechaOperacion"
				,GAR."INGFechaLiquidacion" as "FechaLiquidacion"
				,CAR."IdBoveda"
				,GAR."IdTipoInstrumento"
				,GAR."IdInstrumento"
				,GAR."Nemotecnico"
				,CAR."INGCantidad" AS "CantidadOriginal"
				,GAR."INGCantidad" AS "CantidadGarantia"
				,GAR."IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,CAR."INGTasaCompra" AS "TasaCompra"
				,CAR."INGPrecioCompra" AS "PrecioCompra"
				,GAR."INGMontoATasaGarantiaActual" AS "MontoCompraMonedaSistema"
				,GAR."INGTasaMercadoActual" AS "TasaMercado"
				,GAR."INGPrecioMercadoActual" AS "PrecioMercado"
				,GAR."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaSistema"
				,GAR."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,GAR."INGMontoInteresCorridoMercadoActual" AS "MontoInteresCorridoMercadoActualMonedaSistema"
				,GAR."INGMontoInteresCorridoMercadoActualMonedaAdicional" AS "MontoInteresCorridoMercadoActualMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,GAR."TieneCorteDeCupon"
				,GAR."INGFechaPagoCorteCupon" AS "FechaPagoCorteCupon"
				,CASE WHEN GAR."INGFechaPagoCorteCupon" <= :FechaHasta THEN GAR."INGMontoCapitalCorteCupon" ELSE TO_DECIMAL(0,26,8) END as "CuponesPagadosMonedaSistema"
				,COALESCE(((GAR."INGCantidad" * CAR."MontoCompra") / NULLIF(CAR."Cantidad",0)),0) AS "MontoCompraInicial"
				,CAR."IdClasificacionTipoInstrumento"
		FROM "BSCL.Custodia.BD::HIS.Garantia" AS GAR
		INNER JOIN GarantiaPaso AS REG ON (REG."IdGarantiaHistorico" = GAR."IdGarantiaHistorico") 
		INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" AS CAR ON CAR."IdCartera" = GAR."IdCarteraOrigen" AND GAR."FechaRespaldo" = CAR."FechaRespaldo" AND CAR."IdCartera" = REG."IdCarteraOrigen"
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"

		WHERE 
	
		   CAR."FechaRespaldo" = :FechaHasta
		  AND GAR."IdClasificacionTipoInstrumento" = 'RF'
		  AND GAR."IdEstado" = 'VIG'
		  AND GAR."IdEmpresa" = :IdEmpresa
		  AND GAR."INGFechaOperacion" <= :FechaHasta
	)
	SELECT   "IdCliente","IdPortafolio","FolioCartera",MAX("FechaOperacion") AS "FechaOperacion",MAX("FechaLiquidacion") AS "FechaLiquidacion","IdBoveda","IdTipoInstrumento"
			,"IdInstrumento","Nemotecnico","IdMonedaInstrumento",MAX("CantidadOriginal") AS "CantidadOriginal","TasaCompra","PrecioCompra"
			,"TasaMercado",MAX("PrecioMercado") AS "PrecioMercado","TieneCorteDeCupon","FechaPagoCorteCupon"
			,IFNULL(SUM("MontoCompraMonedaSistema"),TO_DECIMAL(0,26,8)) AS "MontoCompraMonedaSistema"
			,IFNULL(SUM("CantidadGarantia"),TO_DECIMAL(0,26,8)) AS "CantidadGarantia"
			,IFNULL(SUM("MontoMercadoMonedaSistema"),TO_DECIMAL(0,26,8)) AS "MontoMercadoMonedaSistema"
			,IFNULL(SUM("MontoMercadoMonedaAdicional"),TO_DECIMAL(0,26,8)) AS "MontoMercadoMonedaAdicional"
			,IFNULL(SUM("MontoInteresCorridoMercadoActualMonedaSistema"),TO_DECIMAL(0,26,8)) AS "MontoInteresCorridoMercadoActualMonedaSistema"
			,IFNULL(SUM("MontoInteresCorridoMercadoActualMonedaAdicional"),TO_DECIMAL(0,26,8)) AS "MontoInteresCorridoMercadoActualMonedaAdicional"			
			,IFNULL(SUM("CuponesPagadosMonedaSistema"),TO_DECIMAL(0,26,8)) AS "CuponesPagadosMonedaSistema"
			,IFNULL(SUM("MontoCompraInicial"),TO_DECIMAL(0,26,8)) AS "MontoCompraInicial"
			,"IdClasificacionTipoInstrumento"
	FROM Garantia
	GROUP BY "IdCliente","IdPortafolio","FolioCartera","IdBoveda","IdTipoInstrumento","IdInstrumento"
			,"Nemotecnico","IdMonedaInstrumento","TasaCompra","PrecioCompra"
			,"TasaMercado","TieneCorteDeCupon","FechaPagoCorteCupon","IdClasificacionTipoInstrumento";