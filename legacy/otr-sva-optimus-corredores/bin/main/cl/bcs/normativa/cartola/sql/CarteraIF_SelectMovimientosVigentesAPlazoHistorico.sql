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
			,CAR."FechaRespaldo"
		FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
	),
	CarteraExc AS (	
		SELECT C."IdCartera",C."FolioCartera",C."IdCliente",C."IdPortafolio"
		FROM CarteraPortafolioPaso C
		WHERE C."FechaRespaldo" = :FechaHasta
		  AND C."IdEmpresa" = :IdEmpresa
		  AND C."IdTipoInstrumento" IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		  AND C."IdEstado" = 'LIQ'
		AND NOT EXISTS(
			SELECT  CAR."IdCartera",CAR."FolioCartera",CAR."IdCliente",CAR."IdPortafolio"
			FROM CarteraPortafolioPaso CAR
			WHERE CAR."FechaRespaldo" = :FechaHasta
			AND CAR."IdClasificacionTipoInstrumento" = 'IF'
			AND CAR."IdEstado" = 'VIG'
			AND CAR."IdEmpresa" = :IdEmpresa
			AND CAR."INGFechaOperacion" <= :FechaHasta
			AND CAR."IdTipoInstrumento" IN ('SIM','PRT','EMP','PAC','RRV','RRF')
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
				,PLZ."IdEmisor"
				,PLZ."FechaVencimientoInstrumento"
				,PLZ."IdTipoReajuste"
				,PLZ."FechaEmisionInstrumento"
				,PLZ."Nemotecnico"
				,CAR."INGCantidad" AS "CantidadOriginal"
				,CAR."INGValorRescate" AS "ValorRescateOriginal"
				,PLZ."INGCantidad" AS "CantidadAPlazo"
				,PLZ."INGValorRescate" AS "ValorRescateAPlazo"
				,PLZ."IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,CAR."INGTasaCompra" AS "TasaCompra" 
				,CAR."INGPrecioCompra" AS "PrecioCompra"
				,PLZ."INGMontoATasaCompraActual" AS "MontoCompraMonedaSistema"
				,PLZ."INGMontoATasaCompraActual" AS "MontoCompraMonedaAdicional"
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
		FROM "BSCL.Custodia.BD::HIS.Subyacente" PLZ
		INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" CAR ON CAR."FolioCartera" = PLZ."FolioCarteraRelacionada" AND CAR."FechaRespaldo" = PLZ."FechaRespaldo"
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		INNER JOIN CarteraExc AS CEX ON CEX."IdCartera" = CAR."IdCartera"
		WHERE PLZ."FechaRespaldo" = :FechaHasta
		  AND PLZ."IdClasificacionTipoInstrumento" = 'IF'
		  AND PLZ."IdEstado" = 'VIG'
		  AND PLZ."INGFechaOperacion" <= :FechaHasta
	)
	SELECT   "IdCliente","IdPortafolio","FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento"
			,"IdInstrumento","IdEmisor","FechaVencimientoInstrumento","IdTipoReajuste","FechaEmisionInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","ValorRescateOriginal","TasaCompra","PrecioCompra"
			,"MontoCompraMonedaSistema","MontoCompraMonedaAdicional","TasaMercado","PrecioMercado","TieneCorteDeCupon","FechaPagoCorteCupon"
			,IFNULL(SUM("CantidadAPlazo"),TO_DECIMAL(0,26,8)) AS "CantidadAPlazo"
			,IFNULL(SUM("ValorRescateAPlazo"),TO_DECIMAL(0,26,8)) AS "ValorRescateAPlazo"
			,IFNULL(SUM("MontoMercadoMonedaSistema"),TO_DECIMAL(0,26,8)) AS "MontoMercadoMonedaSistema"
			,IFNULL(SUM("MontoMercadoMonedaAdicional"),TO_DECIMAL(0,26,8)) AS "MontoMercadoMonedaAdicional"
			,IFNULL(SUM("MontoInteresCorridoMercadoActualMonedaSistema"),TO_DECIMAL(0,26,8)) AS "MontoInteresCorridoMercadoActualMonedaSistema"
			,IFNULL(SUM("MontoInteresCorridoMercadoActualMonedaAdicional"),TO_DECIMAL(0,26,8)) AS "MontoInteresCorridoMercadoActualMonedaAdicional"			
			,IFNULL(SUM("CuponesPagadosMonedaSistema"),TO_DECIMAL(0,26,8)) AS "CuponesPagadosMonedaSistema"
	FROM APlazo
	GROUP BY "IdCliente","IdPortafolio","FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento","IdInstrumento"
			,"IdEmisor","FechaVencimientoInstrumento","Nemotecnico","IdTipoReajuste","FechaEmisionInstrumento","IdMonedaInstrumento","CantidadOriginal","ValorRescateOriginal","TasaCompra","PrecioCompra"
			,"MontoCompraMonedaSistema","MontoCompraMonedaAdicional","TasaMercado","PrecioMercado","TieneCorteDeCupon","FechaPagoCorteCupon";