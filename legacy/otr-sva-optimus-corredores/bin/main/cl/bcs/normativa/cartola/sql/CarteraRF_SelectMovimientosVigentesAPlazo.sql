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
		SELECT C."IdCartera",C."IdCliente",C."IdPortafolio"
		FROM CarteraPortafolioPaso C
		WHERE 
		
		   C."IdEmpresa" = :IdEmpresa
		  AND C."IdTipoInstrumento" IN ('SIM','PRT','EMP','PAC')
		  AND C."IdEstado" = 'LIQ'
		AND NOT EXISTS (
			SELECT  CAR."IdCartera",CAR."IdCliente",CAR."IdPortafolio"
			FROM CarteraPortafolioPaso CAR
			WHERE
			"IdClasificacionTipoInstrumento" = 'RF'
			AND "IdEstado" = 'VIG'
			AND "IdEmpresa" = :IdEmpresa
			AND "INGFechaOperacion" <= :FechaHasta
			AND "IdTipoInstrumento" IN ('SIM','PRT','EMP','PAC')
		) 
	),
	APlazo AS (
		SELECT 	 CAR."IdCliente"
				,CAR."IdPortafolio"
				,CAR."FolioCartera"
				,PLZ."INGFechaOperacion" AS "FechaOperacion"
				,CAR."INGFechaLiquidacion" as "FechaLiquidacion"
				,CAR."IdBoveda"
				,PLZ."IdTipoInstrumento"
				,PLZ."IdInstrumento"
				,PLZ."Nemotecnico"
				,CAR."INGCantidad" AS "CantidadOriginal"
				,PLZ."Cantidad" AS "CantidadAPlazo"
				,PLZ."IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,CAR."INGTasaCompra" AS "TasaCompra" 
				,CAR."INGPrecioCompra" AS "PrecioCompra"
				,CAR."INGMontoATasaCompraActual" AS "MontoCompraMonedaSistema"
				,PLZ."INGTasaMercadoActual" AS "TasaMercado"
				,PLZ."INGPrecioMercadoActual" as "PrecioMercado"
				,PLZ."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaSistema"
				,PLZ."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,PLZ."INGMontoInteresCorridoMercadoActual" AS "MontoInteresCorridoMercadoActualMonedaSistema"
				,PLZ."INGMontoInteresCorridoMercadoActualMonedaAdicional" AS "MontoInteresCorridoMercadoActualMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,PLZ."TieneCorteDeCupon"
				,PLZ."INGFechaPagoCorteCupon" AS "FechaPagoCorteCupon"
				,CASE WHEN PLZ."INGFechaPagoCorteCupon" <= :FechaHasta THEN PLZ."INGMontoCapitalCorteCupon" ELSE TO_DECIMAL(0,26,8) END as "CuponesPagadosMonedaSistema"
				,COALESCE(((PLZ."INGCantidad" * CAR."MontoCompra") / NULLIF(CAR."Cantidad",0)),0) AS "MontoCompraInicial"
				,CAR."IdClasificacionTipoInstrumento"
		FROM "BSCL.Custodia.BD::CUS.Subyacente" PLZ
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON CAR."FolioCartera" = PLZ."FolioCarteraRelacionada"
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		INNER JOIN CarteraExc AS CEX ON CEX."IdCartera" = CAR."IdCartera"
		WHERE 
		 
		  PLZ."IdClasificacionTipoInstrumento" = 'RF'
		  AND PLZ."IdEstado" = 'VIG'
		  AND PLZ."INGFechaOperacion" <= :FechaHasta
	)
	SELECT   "IdCliente","IdPortafolio","FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento"
			,"IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","TasaCompra","PrecioCompra"
			,"MontoCompraMonedaSistema","TasaMercado","PrecioMercado","TieneCorteDeCupon","FechaPagoCorteCupon"
			,IFNULL(SUM("CantidadAPlazo"),TO_DECIMAL(0,26,8)) AS "CantidadAPlazo"
			,IFNULL(SUM("MontoMercadoMonedaSistema"),TO_DECIMAL(0,26,8)) AS "MontoMercadoMonedaSistema"
			,IFNULL(SUM("MontoMercadoMonedaAdicional"),TO_DECIMAL(0,26,8)) AS "MontoMercadoMonedaAdicional"
			,IFNULL(SUM("MontoInteresCorridoMercadoActualMonedaSistema"),TO_DECIMAL(0,26,8)) AS "MontoInteresCorridoMercadoActualMonedaSistema"
			,IFNULL(SUM("MontoInteresCorridoMercadoActualMonedaAdicional"),TO_DECIMAL(0,26,8)) AS "MontoInteresCorridoMercadoActualMonedaAdicional"			
			,IFNULL(SUM("CuponesPagadosMonedaSistema"),TO_DECIMAL(0,26,8)) AS "CuponesPagadosMonedaSistema"
			,IFNULL(SUM("MontoCompraInicial"),TO_DECIMAL(0,26,8)) AS "MontoCompraInicial"
			,"IdClasificacionTipoInstrumento"
	FROM APlazo
	GROUP BY "IdCliente","IdPortafolio","FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento","IdInstrumento"
			,"Nemotecnico","IdMonedaInstrumento","CantidadOriginal","TasaCompra","PrecioCompra","MontoCompraMonedaSistema","TasaMercado","PrecioMercado"
			,"TieneCorteDeCupon","FechaPagoCorteCupon","IdClasificacionTipoInstrumento";