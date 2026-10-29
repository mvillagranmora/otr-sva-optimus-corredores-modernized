WITH 
	Cartera AS (
		SELECT 	 
			"IdCliente"
			,CAR."IdPortafolio"
			,"FolioCartera"
			,"FechaOperacion"
			,"FechaLiquidacion"
			,"FechaRespaldo"
			,"IdBoveda"
			,"IdEstado"
			,"IdEmpresa"
			,"IdTipoInstrumento"
			,"IdInstrumento"
			,"Nemotecnico"	
			,"Cantidad" AS "CantidadOriginal"
			,"INGCantidad"  AS "CantidadCartera"
			,"IdMonedaInstrumento"
			--------------------------------------------------------------------------------------------
			,"INGTasaCompra" AS "TasaCompra"
			,"INGPrecioCompra" AS "PrecioCompra"
			,"INGMontoATasaCompraActual" AS "MontoCompraMonedaSistema"
			,"INGTasaMercadoActual" AS "TasaMercado"
			,"INGPrecioMercadoActual" AS "PrecioMercado"
			,"INGMontoATasaMercadoActual" AS "MontoMercadoMonedaSistema"
			,"INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
			,"INGFechaOperacion"
			---------------------------------------------------------------------------------------------
			,"INGMontoInteresCorridoMercadoActual" AS "MontoInteresCorridoMercadoActualMonedaSistema"
			,"INGMontoInteresCorridoMercadoActualMonedaAdicional" AS "MontoInteresCorridoMercadoActualMonedaAdicional"
			---------------------------------------------------------------------------------------------
			,"TieneCorteDeCupon"
			,"INGFechaPagoCorteCupon" AS "FechaPagoCorteCupon" 			
			,CASE WHEN "INGFechaPagoCorteCupon" <= :FechaHasta THEN "INGMontoCapitalCorteCupon" ELSE TO_DECIMAL(0,26,8) END as "CuponesPagadosMonedaSistema"
			,COALESCE(((CAR."INGCantidad" * CAR."MontoCompra") / NULLIF(CAR."Cantidad",0)),0) AS "MontoCompraInicial"
			,"IdClasificacionTipoInstrumento"
		FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		WHERE
		
		 CAR."FechaRespaldo" = :FechaHasta
		AND CAR."IdClasificacionTipoInstrumento" = 'RF'
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."INGFechaOperacion" <= :FechaHasta
	),
	CarteraFiltrada AS (
		SELECT 	 
			"IdCliente"
			,"IdPortafolio"
			,"FolioCartera"
			,"FechaOperacion"
			,"FechaLiquidacion"
			,"IdBoveda"
			,"IdTipoInstrumento"
			,"IdInstrumento"
			,"Nemotecnico"	
			,"CantidadOriginal"
			,"CantidadCartera"
			,"IdMonedaInstrumento"
			--------------------------------------------------------------------------------------------
			,"TasaCompra"
			,"PrecioCompra"
			,"MontoCompraMonedaSistema"
			,"TasaMercado"
			,"PrecioMercado"
			,"MontoMercadoMonedaSistema"
			,"MontoMercadoMonedaAdicional"
			,"INGFechaOperacion"
			---------------------------------------------------------------------------------------------
			,"MontoInteresCorridoMercadoActualMonedaSistema"
			,"MontoInteresCorridoMercadoActualMonedaAdicional"
			---------------------------------------------------------------------------------------------
			,"TieneCorteDeCupon"
			,"FechaPagoCorteCupon" 			
			,"CuponesPagadosMonedaSistema"
			,"MontoCompraInicial"
			,"IdClasificacionTipoInstrumento"
		FROM Cartera
		WHERE "IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF', 'ERF', 'PRF')
	), 
	OperacionesAPlazoAsociadoACartera AS (
		SELECT   SUB."IdSubyacente"
				,CAR."FolioCartera"
				,SUM(SUB."INGCantidad") AS "CantidadAPlazo"
		FROM Cartera CAR
		INNER JOIN "BSCL.Custodia.BD::HIS.Subyacente" SUB ON CAR."FolioCartera" = SUB."FolioCarteraRelacionada" AND CAR."FechaRespaldo" = SUB."FechaRespaldo"	
		WHERE 
	
		CAR."FechaRespaldo" = :FechaHasta
		AND CAR."IdClasificacionTipoInstrumento" = 'RF'
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."INGFechaOperacion" <= :FechaHasta
		AND SUB."IdEstado" = 'VIG'
		AND CAR."IdTipoInstrumento" IN ('SIM','PRT','EMP','PAC', 'ERF', 'PRF')
		GROUP BY SUB."IdSubyacente",CAR."FolioCartera"
	),
	ReporteAsociadoACartera AS (
		SELECT   SUB."IdSubyacente"
				,CAR."FolioCartera"
				,SUM(SUB."INGCantidad") AS "CantidadReporte"
		FROM Cartera CAR
		INNER JOIN "BSCL.Custodia.BD::HIS.Subyacente" SUB ON CAR."FolioCartera" = SUB."FolioCarteraRelacionada"	AND CAR."FechaRespaldo" = SUB."FechaRespaldo"
		WHERE 
	
		CAR."FechaRespaldo" = :FechaHasta
		AND CAR."IdClasificacionTipoInstrumento" = 'RF'
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."INGFechaOperacion" <= :FechaHasta
		AND SUB."IdEstado" = 'VIG'
		AND CAR."IdTipoInstrumento" IN ('RRV','RRF')
		GROUP BY SUB."IdSubyacente",CAR."FolioCartera"
	),
	ConsultarOperacionesMesa as (
		SELECT OP."IdTipoOperacion",
				SUM(SUB."INGMontoMercadoActualMonedaInstrumento") AS "MontoCompraDetalle",
				SUM(RE."CantidadInicial") AS "CantidadDetalle",
				RE."Nemotecnico" AS "NemotecnicoDetalle",
				OP."IdCliente", 
				OP."IdPortafolio"
			FROM "BSCL.Mesa.BD::MES.Operacion" OP
				INNER JOIN "BSCL.Custodia.BD::HIS.Reserva" RE ON OP."IdOperacion" = RE."IdOperacion" 
				INNER JOIN "BSCL.Custodia.BD::HIS.Subyacente" SUB ON re."FolioSubyacente" = SUB."FolioSubyacente" 
			WHERE OP."IdTipoOperacion" IN ('VENPAC', 'COMPAC') AND RE."FechaRespaldo" <= :FechaHasta AND SUB."FechaRespaldo" <= :FechaHasta AND SUB."IdEstado" = 'VIG' AND RE."IdEstado" = 'VIG'
			GROUP BY RE."Nemotecnico", OP."IdTipoOperacion", OP."IdCliente", OP."IdPortafolio"
	)
	SELECT   C."IdCliente"
			,C."IdPortafolio"
			,C."FolioCartera"
			,C."FechaOperacion"
			,C."FechaLiquidacion"
			,C."IdTipoInstrumento"
			,C."IdBoveda"
			,C."IdInstrumento"
			,C."Nemotecnico"
			,IFNULL(C."CantidadOriginal",TO_DECIMAL(0,26,8)) AS "CantidadOriginal"
			,IFNULL(C."CantidadCartera",TO_DECIMAL(0,26,8)) AS "CantidadCartera"
			,TO_DECIMAL(0,26,8) AS "CantidadGarantia"
			,CASE WHEN COM."IdTipoOperacion" = 'COMPAC'  
				THEN  COM."CantidadDetalle"
			WHEN COM."IdTipoOperacion" = 'VENPAC'
				THEN IFNULL(C."CantidadCartera",TO_DECIMAL(0,26,8))*-1 
			ELSE  IFNULL(P."CantidadAPlazo",TO_DECIMAL(0,26,8)) END  AS "CantidadAPlazo"
			,IFNULL(R."CantidadReporte",TO_DECIMAL(0,26,8)) AS "CantidadReporte"
			,C."IdMonedaInstrumento"
			--------------------------------------------------------------------------------------------
			,C."TasaCompra"
			,C."PrecioCompra"
			,C."MontoCompraMonedaSistema"
			,C."TasaMercado"
			,C."PrecioMercado"
			,CASE WHEN COM."IdTipoOperacion" = 'COMPAC' 
				THEN  C."MontoMercadoMonedaSistema" + COM."MontoCompraDetalle"
			WHEN COM."IdTipoOperacion" = 'VENPAC'
				THEN C."MontoMercadoMonedaSistema" - C."MontoMercadoMonedaSistema"
			ELSE  C."MontoMercadoMonedaSistema" END  AS "MontoMercadoMonedaSistema"
			,C."MontoMercadoMonedaAdicional"
			---------------------------------------------------------------------------------------------
			,C."MontoInteresCorridoMercadoActualMonedaSistema"
			,C."MontoInteresCorridoMercadoActualMonedaAdicional"
			---------------------------------------------------------------------------------------------
			,C."TieneCorteDeCupon"
			,C."FechaPagoCorteCupon"			
			,C."CuponesPagadosMonedaSistema"
			,C."MontoCompraInicial"
			,C."IdClasificacionTipoInstrumento"
	FROM CarteraFiltrada AS C
	LEFT JOIN ConsultarOperacionesMesa COM 
		ON C."IdCliente" = COM."IdCliente" 
		AND C."IdPortafolio" = COM."IdPortafolio" 
		AND C."Nemotecnico" = COM."NemotecnicoDetalle"
	FULL OUTER JOIN OperacionesAPlazoAsociadoACartera P ON P."FolioCartera" = C."FolioCartera"
	FULL OUTER JOIN ReporteAsociadoACartera R ON R."FolioCartera" = C."FolioCartera";