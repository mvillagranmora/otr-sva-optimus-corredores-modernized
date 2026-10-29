WITH
	CarteraPortafolioPaso AS (
		SELECT
			CAR."IdCliente"
			,CAR."IdEmpresa"
			,CAR."IdCartera"
			,CAR."IdPortafolio"
			,CAR."FolioCartera"
			,CAR."INGFechaLiquidacion"
			,CAR."IdBoveda"
			,CAR."IdClasificacionTipoInstrumento"
			,CAR."IdTipoInstrumento"
			,CAR."IdEstado"
			,CAR."INGCantidad"
			,CAR."INGValorRescate"
			,CAR."INGTasaCompra"
			,CAR."INGPrecioCompra"
			,CAR."INGFechaOperacion"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
	),
	CarteraExc AS (
		SELECT C."IdCartera",C."FolioCartera",C."IdCliente",C."IdPortafolio"
		FROM CarteraPortafolioPaso C
		WHERE 
		 
		   C."IdEmpresa" = :IdEmpresa
		  AND C."IdTipoInstrumento" IN ('RRV','RRF')
		  AND C."IdEstado" = 'LIQ'
		AND NOT EXISTS (
			SELECT CAR."IdCartera",CAR."FolioCartera",CAR."IdCliente",CAR."IdPortafolio"
			FROM CarteraPortafolioPaso CAR
			WHERE 
			CAR."IdClasificacionTipoInstrumento" = 'RF'
			AND CAR."IdEstado" = 'VIG'
			AND CAR."IdEmpresa" = :IdEmpresa
			AND CAR."INGFechaOperacion" <= :FechaHasta
			AND CAR."IdTipoInstrumento" IN ('RRV','RRF')
		) 
	),
	Reporte AS (
		SELECT 	 CAR."IdCliente"
				,CAR."IdPortafolio"
				,CAR."FolioCartera"
				,REP."INGFechaOperacion" AS "FechaOperacion"
				,CAR."INGFechaLiquidacion" as "FechaLiquidacion"
				,CAR."IdBoveda"
				,REP."IdTipoInstrumento"
				,REP."IdInstrumento"
				,REP."Nemotecnico"
				,CAR."INGCantidad" AS "CantidadOriginal"
				,REP."Cantidad" AS "CantidadReporte"
				,REP."IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,CAR."INGTasaCompra" AS "TasaCompra"
				,CAR."INGPrecioCompra" AS "PrecioCompra"
				,CAR."INGMontoATasaCompraActual" AS "MontoCompraMonedaSistema"
				,REP."INGTasaMercadoActual" AS "TasaMercado"
				,REP."INGPrecioMercadoActual" as "PrecioMercado"
				,REP."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaSistema"
				,REP."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,REP."INGMontoInteresCorridoMercadoActual" AS "MontoInteresCorridoMercadoActualMonedaSistema"
				,REP."INGMontoInteresCorridoMercadoActualMonedaAdicional" AS "MontoInteresCorridoMercadoActualMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,REP."TieneCorteDeCupon"
				,REP."INGFechaPagoCorteCupon" AS "FechaPagoCorteCupon"
				,CASE WHEN REP."INGFechaPagoCorteCupon" <= :FechaHasta THEN REP."INGMontoCapitalCorteCupon" ELSE TO_DECIMAL(0,26,8) END as "CuponesPagadosMonedaSistema"
				,COALESCE(((REP."INGCantidad" * CAR."MontoCompra") / NULLIF(CAR."Cantidad",0)),0) AS "MontoCompraInicial"
				,CAR."IdClasificacionTipoInstrumento"
		FROM "BSCL.Custodia.BD::CUS.Subyacente" REP
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON CAR."FolioCartera" = REP."FolioCarteraRelacionada"
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		INNER JOIN CarteraExc AS CEX ON CEX."IdCartera" = CAR."IdCartera"
		WHERE 
		 
		  REP."IdClasificacionTipoInstrumento" = 'RF'
		  AND REP."IdEstado" = 'VIG'
		  AND REP."INGFechaOperacion" <= :FechaHasta
	)
	SELECT   "IdCliente","IdPortafolio","FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento"
			,"IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","TasaCompra","PrecioCompra"
			,"MontoCompraMonedaSistema","TasaMercado","PrecioMercado","TieneCorteDeCupon","FechaPagoCorteCupon"
			,IFNULL(SUM("CantidadReporte"),TO_DECIMAL(0,26,8)) AS "CantidadReporte"
			,IFNULL(SUM("MontoMercadoMonedaSistema"),TO_DECIMAL(0,26,8)) AS "MontoMercadoMonedaSistema"
			,IFNULL(SUM("MontoMercadoMonedaAdicional"),TO_DECIMAL(0,26,8)) AS "MontoMercadoMonedaAdicional"
			,IFNULL(SUM("MontoInteresCorridoMercadoActualMonedaSistema"),TO_DECIMAL(0,26,8)) AS "MontoInteresCorridoMercadoActualMonedaSistema"
			,IFNULL(SUM("MontoInteresCorridoMercadoActualMonedaAdicional"),TO_DECIMAL(0,26,8)) AS "MontoInteresCorridoMercadoActualMonedaAdicional"			
			,IFNULL(SUM("CuponesPagadosMonedaSistema"),TO_DECIMAL(0,26,8)) AS "CuponesPagadosMonedaSistema"
			,IFNULL(SUM("MontoCompraInicial"),TO_DECIMAL(0,26,8)) AS "MontoCompraInicial"
			,"IdClasificacionTipoInstrumento"
	FROM Reporte
	GROUP BY "IdCliente","IdPortafolio","FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento","IdInstrumento"
			,"Nemotecnico","IdMonedaInstrumento","CantidadOriginal","TasaCompra","PrecioCompra","MontoCompraMonedaSistema","TasaMercado","PrecioMercado"
			,"TieneCorteDeCupon","FechaPagoCorteCupon","IdClasificacionTipoInstrumento";