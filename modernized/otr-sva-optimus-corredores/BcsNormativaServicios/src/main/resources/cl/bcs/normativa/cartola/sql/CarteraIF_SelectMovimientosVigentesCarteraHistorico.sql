WITH 
	Cartera AS (
		SELECT 	 
			 CAR."IdCliente"
			,CAR."IdPortafolio"
			,CAR."IdCartera"
			,CAR."FolioCartera"
			,CAR."INGFechaOperacion" AS "FechaOperacion"
			,CAR."INGFechaLiquidacion" as "FechaLiquidacion"
			,CAR."IdBoveda"
			,CAR."IdEstado"
			,CAR."IdEmpresa"
			,CAR."FechaRespaldo"
			,CAR."IdTipoInstrumento"
			,CAR."IdInstrumento"
			,CAR."IdEmisor"
			,CAR."FechaVencimientoInstrumento"
			,CAR."IdTipoReajuste"
			,CAR."FechaEmisionInstrumento"
			,CAR."NemoSVS" AS "Nemotecnico"
			,CAR."Cantidad" AS "CantidadOriginal"
			,CAR."ValorRescate" AS "ValorRescateOriginal"
			,CAR."INGCantidad" AS "CantidadCartera"
			,CAR."INGValorRescate" AS "ValorRescateCartera"
			,CAR."IdMonedaInstrumento"
			--------------------------------------------------------------------------------------------
			,CAR."INGTasaCompra" AS "TasaCompra"
			,CAR."INGPrecioCompra" AS "PrecioCompra"
			,CAR."MontoCompra" AS "MontoCompraMonedaSistema"
			,CAR."INGMontoATasaCompraActual" AS "MontoCompraMonedaAdicional"
			,CAR."INGTasaMercadoActual" AS "TasaMercado"
			,CAR."INGPrecioMercadoActual" AS "PrecioMercado"
			,CAR."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaSistema"
			,CAR."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
			,CAR."INGFechaOperacion"
			---------------------------------------------------------------------------------------------
			,CAR."INGMontoInteresCorridoMercadoActual" AS "MontoInteresCorridoMercadoActualMonedaSistema" 
			,CAR."INGMontoInteresCorridoMercadoActualMonedaAdicional" AS "MontoInteresCorridoMercadoActualMonedaAdicional"
			---------------------------------------------------------------------------------------------
			,CAR."TieneCorteDeCupon"
			,CAR."INGFechaPagoCorteCupon" AS "FechaPagoCorteCupon" 
			,CASE WHEN CAR."INGFechaPagoCorteCupon" <= :FechaHasta THEN CAR."INGMontoCapitalCorteCupon" ELSE 0 END as "CuponesPagadosMonedaSistema"
			,CAR."IdClasificacionTipoInstrumento"
			,CAR."IdOperacion"
		FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		WHERE  CAR."FechaRespaldo" = :FechaHasta
		AND CAR."IdClasificacionTipoInstrumento" = 'IF'
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."INGFechaOperacion" <= :FechaHasta
	),
	CarteraFiltrada AS (
		SELECT 	 
			 "IdCliente"
			,"IdPortafolio"
			,"IdCartera"
			,"FolioCartera"
			,"FechaOperacion"
			,"FechaLiquidacion"
			,"IdBoveda"
			,"IdEstado"
			,"IdEmpresa"
			,"IdTipoInstrumento"
			,"IdInstrumento"
			,"IdEmisor"
			,"FechaVencimientoInstrumento"
			,"IdTipoReajuste"
			,"FechaEmisionInstrumento"
			,"Nemotecnico"
			,"CantidadOriginal"
			,"ValorRescateOriginal"
			,"CantidadCartera"
			,"ValorRescateCartera"
			,"IdMonedaInstrumento"
			--------------------------------------------------------------------------------------------
			,"TasaCompra"
			,"PrecioCompra"
			,"MontoCompraMonedaSistema"
			,"MontoCompraMonedaAdicional"
			,"TasaMercado"
			,"PrecioMercado"
			,"MontoMercadoMonedaSistema"
			,"MontoMercadoMonedaAdicional"
			---------------------------------------------------------------------------------------------
			,"MontoInteresCorridoMercadoActualMonedaSistema" 
			,"MontoInteresCorridoMercadoActualMonedaAdicional"
			---------------------------------------------------------------------------------------------
			,"TieneCorteDeCupon"
			,"FechaPagoCorteCupon" 
			,"CuponesPagadosMonedaSistema"
			,"IdClasificacionTipoInstrumento"
		FROM Cartera
		WHERE "IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF','ERF', 'PRF')
	),
	GarantiaAsociadaACartera AS (
		SELECT   CAR."FolioCartera"
				,SUM(GAR."INGCantidad") AS "CantidadGarantia"
				,SUM(GAR."INGValorRescate") AS "ValorRescateGarantia"
				,SUM(GAR."INGMontoATasaMercadoActual") AS "MontoMercadoGarantiaActual"
				,SUM(GAR."INGMontoATasaGarantiaActual") AS "MontoGarantiaValorizadoActual"
		FROM Cartera CAR
		INNER JOIN "BSCL.Custodia.BD::HIS.Garantia" GAR 
				ON GAR."FolioCarteraOrigen" = CAR."FolioCartera" AND CAR."FechaRespaldo" = GAR."FechaRespaldo"
		WHERE CAR."FechaRespaldo" = :FechaHasta
		AND CAR."IdClasificacionTipoInstrumento" = 'IF'
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."INGFechaOperacion" <= :FechaHasta
		AND GAR."IdEstado" = 'VIG'
		GROUP BY CAR."FolioCartera"
	),
	OperacionesAPlazoAsociadoACartera AS (
		SELECT  CAR."FolioCartera"
				,SUM(SUB."INGCantidad") AS "CantidadAPlazo"
				,SUM(SUB."INGValorRescate") AS "ValorRescateAPlazo"
		FROM Cartera CAR
		INNER JOIN "BSCL.Custodia.BD::HIS.Subyacente" SUB ON CAR."FolioCartera" = SUB."FolioCarteraRelacionada"	AND CAR."FechaRespaldo" = SUB."FechaRespaldo"
		WHERE  CAR."FechaRespaldo" = :FechaHasta
		  AND SUB."IdClasificacionTipoInstrumento" = 'IF'
	  	  AND CAR."IdEstado" = 'VIG'
	  	  AND CAR."IdEmpresa" = :IdEmpresa
	  	  AND CAR."INGFechaOperacion" <= :FechaHasta	
		  AND SUB."IdEstado" = 'VIG'
		  AND CAR."IdTipoInstrumento" IN ('SIM','PRT','EMP','PAC','RRV','RRF', 'ERF', 'PRF')
		GROUP BY CAR."FolioCartera"
	), ConsultarOperacionesMesa AS ( SELECT 
		OP."IdTipoOperacion",
		SUM(SUB."INGMontoMercadoActualMonedaInstrumento") AS "MontoCompraDetalle",
		SUM(RE."ValorRescateInicial") AS "ValorRescateDetalle",
		SUB."NemoSVS"  AS "NemotecnicoDetalle",
		OP."IdCliente", 
		OP."IdPortafolio"
	FROM Cartera CAR
	INNER JOIN "BSCL.Mesa.BD::MES.Operacion" OP ON OP."IdOperacion" = CAR."IdOperacion"
	INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" RE ON OP."IdOperacion" = RE."IdOperacion"
	INNER JOIN "BSCL.Custodia.BD::HIS.Subyacente" SUB ON re."FolioSubyacente" = SUB."FolioSubyacente" AND CAR."FechaRespaldo" = SUB."FechaRespaldo"
	WHERE 
		OP."IdTipoOperacion" IN ('VENPAC', 'COMPAC') 
		AND OP."IdEstado" = 'VIG'
		AND SUB."IdClasificacionTipoInstrumento" = 'IF'
	GROUP BY
			SUB."NemoSVS",
			OP."IdTipoOperacion",
			OP."IdCliente",
			OP."IdPortafolio"
	) SELECT   C."IdCliente"
			,C."IdPortafolio"
			,C."FolioCartera"
			,C."FechaOperacion"
			,C."FechaLiquidacion"
			,C."IdBoveda"
			,C."IdTipoInstrumento"			
			,C."IdInstrumento"
			,C."IdEmisor"
			,C."FechaVencimientoInstrumento"
			,C."IdTipoReajuste"
			,C."FechaEmisionInstrumento"
			,C."Nemotecnico"
			,IFNULL(C."CantidadOriginal",TO_DECIMAL(0,26,8)) AS "CantidadOriginal"
			,IFNULL(C."ValorRescateOriginal",TO_DECIMAL(0,26,8)) AS "ValorRescateOriginal"
			,IFNULL(C."CantidadCartera",TO_DECIMAL(0,26,8)) AS "CantidadCartera"
			,IFNULL(C."ValorRescateCartera",TO_DECIMAL(0,26,8))	AS "ValorRescateCartera"	
			,IFNULL(G."CantidadGarantia",TO_DECIMAL(0,26,8)) AS "CantidadGarantia"
			,IFNULL(G."ValorRescateGarantia",TO_DECIMAL(0,26,8)) AS "ValorRescateGarantia"
			,IFNULL(P."CantidadAPlazo",TO_DECIMAL(0,26,8)) AS "CantidadAPlazo"
			,CASE WHEN COM."IdTipoOperacion" = 'COMPAC'  
				THEN  COM."ValorRescateDetalle"
			WHEN COM."IdTipoOperacion" = 'VENPAC'
				THEN IFNULL(C."ValorRescateCartera",TO_DECIMAL(0,26,8))*-1 
			ELSE  IFNULL(P."ValorRescateAPlazo",TO_DECIMAL(0,26,8)) END  AS "ValorRescateAPlazo"
			,C."IdMonedaInstrumento"
			--------------------------------------------------------------------------------------------
			,C."TasaCompra"
			,C."PrecioCompra"
			,C."MontoCompraMonedaSistema"
			,C."MontoCompraMonedaAdicional"
			,C."TasaMercado"
			,C."PrecioMercado"
			,CASE WHEN COM."IdTipoOperacion" = 'COMPAC' 
				THEN  C."MontoMercadoMonedaSistema" + COM."MontoCompraDetalle"
			WHEN COM."IdTipoOperacion" = 'VENPAC'
				THEN C."MontoMercadoMonedaSistema" - C."MontoMercadoMonedaSistema"
			ELSE C."MontoMercadoMonedaSistema" END  AS "MontoMercadoMonedaSistema"
			,C."MontoMercadoMonedaAdicional"
			---------------------------------------------------------------------------------------------
			,C."MontoInteresCorridoMercadoActualMonedaSistema"
			,C."MontoInteresCorridoMercadoActualMonedaAdicional"
			---------------------------------------------------------------------------------------------
			,C."TieneCorteDeCupon"
			,C."FechaPagoCorteCupon"			
			,C."CuponesPagadosMonedaSistema"
			,IFNULL(G."MontoMercadoGarantiaActual",TO_DECIMAL(0,26,8)) AS "MontoMercadoGarantiaActual"
			,IFNULL(G."MontoGarantiaValorizadoActual",TO_DECIMAL(0,26,8)) AS "MontoGarantiaValorizadoActual"
	FROM CarteraFiltrada AS C
	LEFT JOIN ConsultarOperacionesMesa COM 
		ON C."IdCliente" = COM."IdCliente" 
		AND C."IdPortafolio" = COM."IdPortafolio" 
		AND C."Nemotecnico" = COM."NemotecnicoDetalle"
	FULL OUTER JOIN GarantiaAsociadaACartera G ON G."FolioCartera" = C."FolioCartera"
	FULL OUTER JOIN OperacionesAPlazoAsociadoACartera P ON P."FolioCartera" = C."FolioCartera";	