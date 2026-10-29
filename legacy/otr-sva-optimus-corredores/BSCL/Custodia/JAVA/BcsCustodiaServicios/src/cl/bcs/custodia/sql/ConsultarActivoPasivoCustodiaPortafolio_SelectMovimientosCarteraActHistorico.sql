WITH 
	Cartera AS
	(
------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------	DISPONIBLE ACTIVO 
------------------------------------------------------------------------------------------------------------------------------------------	
		SELECT 	 
			 CAR."IdCliente"
			,CAR."IdPortafolio"
			,CAR."IdBoveda"
			,CAR."IdClasificacionTipoInstrumento"
			,CAR."IdTipoInstrumento"
			,CAR."IdInstrumento"
			,CAR."Nemotecnico"	
			,SUM(CAR."INGCantidad")							AS "DisponibleActivo"
			,0												AS "DisponiblePasivo"
			,0												AS "GarantiaActivo"
			,0												AS "GarantiaPasivo"
			,0												AS "PrestamoActivo"
			,0												AS "PrestamoPasivo"
			,0												AS "PlazoActivo"
			,0												AS "PlazoPasivo"			
			,CAR."IdMonedaInstrumento"
			,CAR."IdEmisor"
			--------------------------------------------------------------------------------------------
			,CAR."INGPrecioPromedioCompra" 						AS "PrecioPromedioCompra"
			,CAR."INGPrecioMercadoActual" 						AS "PrecioMercado"
			,SUM(CAR."INGMontoPrecioPromedioCompra") 			AS "MontoPromedioCompraMonedaSistemaActivo"
			,SUM(CAR."INGMontoPrecioPromedioMonedaAdicional") 	AS "MontoPromedioCompraMonedaAdicionalActivo"
			,SUM(CAR."INGMontoATasaCompraActual") 				AS "MontoATasaCompraActivo"
			,SUM(CAR."INGMontoATasaMercadoActual") 				AS "MontoMercadoMonedaSistemaActivo"
			,SUM(CAR."INGMontoMercadoActualMonedaAdicional") 	AS "MontoMercadoMonedaAdicionalActivo"
			,0 	AS "MontoPromedioCompraMonedaSistemaPasivo"
			,0 	AS "MontoPromedioCompraMonedaAdicionalPasivo"
			,0 	AS "MontoATasaCompraPasivo"
			,0 	AS "MontoMercadoMonedaSistemaPasivo"
			,0 	AS "MontoMercadoMonedaAdicionalPasivo"
		FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."FechaRespaldo" = :FechaHasta
		AND CAR."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."FechaLiquidacion" <= :FechaHasta
	  	AND CAR."EGRCantidad" = 0
		AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		GROUP BY 
		CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",CAR."IdClasificacionTipoInstrumento",
		CAR."IdTipoInstrumento",CAR."IdInstrumento",CAR."Nemotecnico",CAR."IdMonedaInstrumento",CAR."IdEmisor", CAR."INGPrecioPromedioCompra",CAR."INGPrecioMercadoActual"
		
UNION ALL
------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------	DISPONIBLE PASIVO 
------------------------------------------------------------------------------------------------------------------------------------------	
 
		SELECT 	 
			 CAR."IdCliente"
			,CAR."IdPortafolio"
			,CAR."IdBoveda"
			,CAR."IdClasificacionTipoInstrumento"
			,CAR."IdTipoInstrumento"
			,CAR."IdInstrumento"
			,CAR."Nemotecnico"	
			,0												AS "DisponibleActivo"
			,SUM(CAR."Cantidad")							AS "DisponiblePasivo"
			,0												AS "GarantiaActivo"
			,0												AS "GarantiaPasivo"		
			,0												AS "PrestamoActivo"
			,0												AS "PrestamoPasivo"	
			,0												AS "PlazoActivo"
			,0												AS "PlazoPasivo"			
			,CAR."IdMonedaInstrumento"
			,CAR."IdEmisor"
			--------------------------------------------------------------------------------------------
			,CAR."PrecioCompra"									AS "PrecioPromedioCompra"
			,CAR."PrecioCompra" 								AS "PrecioMercado"
			,0 													AS "MontoPromedioCompraMonedaSistemaActivo"
			,0 													AS "MontoPromedioCompraMonedaAdicionalActivo"
			,0 													AS "MontoATasaCompraActivo"
			,0 													AS "MontoMercadoMonedaSistemaActivo"
			,0 													AS "MontoMercadoMonedaAdicionalActivo"
			,SUM(CAR."MontoCompra") 	AS "MontoPromedioCompraMonedaSistemaPasivo"
			,SUM(CAR."MontoCompraMonedaAdicional") 	AS "MontoPromedioCompraMonedaAdicionalPasivo"
			,SUM(CAR."MontoCompra") 	AS "MontoATasaCompraPasivo"
			,SUM(CAR."MontoCompra") 	AS "MontoMercadoMonedaSistemaPasivo"
			,SUM(CAR."MontoCompraMonedaAdicional") 	AS "MontoMercadoMonedaAdicionalPasivo"
		FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS CAR
		INNER JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV ON CAR."IdMovimientoCarteraEnTransito" = MOV."IdMovimientoCarteraEnTransito"
		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."FechaRespaldo" = :FechaHasta
		AND CAR."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'PEN'
	  	AND CAR."FechaOperacion" <= :FechaHasta
		AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		AND MOV."EsVentaDescubierta" = 'S'
		GROUP BY 
		CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",CAR."IdClasificacionTipoInstrumento",
		CAR."IdTipoInstrumento",CAR."IdInstrumento",CAR."Nemotecnico",CAR."IdMonedaInstrumento",CAR."IdEmisor", CAR."PrecioCompra"  
UNION ALL 		
------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------	GARANTIA ACTIVO 
------------------------------------------------------------------------------------------------------------------------------------------	
		SELECT 	 
			 CAR."IdCliente"
			,CAR."IdPortafolio"
			,CAR."IdBoveda"
			,GAR."IdClasificacionTipoInstrumento"
			,GAR."IdTipoInstrumento"
			,GAR."IdInstrumento"
			,GAR."Nemotecnico"	
			,0												AS "DisponibleActivo"
			,0												AS "DisponiblePasivo"
			,SUM(GAR."INGCantidad")							AS "GarantiaActivo"
			,0												AS "GarantiaPasivo"
			,0												AS "PrestamoActivo"
			,0												AS "PrestamoPasivo"
			,0												AS "PlazoActivo"
			,0												AS "PlazoPasivo"			
			,GAR."IdMonedaInstrumento"
			,GAR."IdEmisor"
			--------------------------------------------------------------------------------------------
			,CAR."INGPrecioPromedioCompra" 						AS "PrecioPromedioCompra"
			,GAR."INGPrecioMercadoActual" 						AS "PrecioMercado"
			,0	AS "MontoPromedioCompraMonedaSistemaActivo"
			,0 	AS "MontoPromedioCompraMonedaAdicionalActivo"
			,0	AS "MontoATasaCompraActivo"
			,0	AS "MontoMercadoMonedaSistemaActivo"
			,0 	AS "MontoMercadoMonedaAdicionalActivo"
			,0 	AS "MontoPromedioCompraMonedaSistemaPasivo"
			,0 	AS "MontoPromedioCompraMonedaAdicionalPasivo"
			,0 	AS "MontoATasaCompraPasivo"
			,0 	AS "MontoMercadoMonedaSistemaPasivo"
			,0 	AS "MontoMercadoMonedaAdicionalPasivo"
		FROM "BSCL.Custodia.BD::HIS.Garantia" GAR
		INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" CAR ON GAR."FolioCarteraOrigen" = CAR."FolioCartera" AND GAR."FechaRespaldo" = CAR."FechaRespaldo"
		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."FechaRespaldo" = :FechaHasta
		AND GAR."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND GAR."IdEstado" = 'VIG'
	  	AND GAR."FechaOperacion" <= :FechaHasta
		GROUP BY 
		CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",GAR."IdClasificacionTipoInstrumento",
		GAR."IdTipoInstrumento",GAR."IdInstrumento",GAR."Nemotecnico",GAR."IdMonedaInstrumento",GAR."IdEmisor", CAR."INGPrecioPromedioCompra", GAR."INGPrecioMercadoActual"	
UNION ALL 		
------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------	PRESTAMO ACTIVO 
------------------------------------------------------------------------------------------------------------------------------------------	
		SELECT 	 
			 CAR."IdCliente"
			,CAR."IdPortafolio"
			,CAR."IdBoveda"
			,SUB."IdClasificacionTipoInstrumento"
			,SUB."IdTipoInstrumento"
			,SUB."IdInstrumento"
			,SUB."Nemotecnico"	
			,0												AS "DisponibleActivo"
			,0												AS "DisponiblePasivo"
			,0												AS "GarantiaActivo"
			,0												AS "GarantiaPasivo"
			,SUM(SUB."INGCantidad") 						AS "PrestamoActivo"
			,0												AS "PrestamoPasivo"		
			,0												AS "PlazoActivo"
			,0												AS "PlazoPasivo"
			,SUB."IdMonedaInstrumento"
			,SUB."IdEmisor"
			--------------------------------------------------------------------------------------------
			,CAR."INGPrecioPromedioCompra" 						AS "PrecioPromedioCompra"
			,SUB."INGPrecioMercadoActual" 						AS "PrecioMercado"
			,0	AS "MontoPromedioCompraMonedaSistemaActivo"
			,0 	AS "MontoPromedioCompraMonedaAdicionalActivo"
			,0	AS "MontoATasaCompraActivo"
			,0	AS "MontoMercadoMonedaSistemaActivo"
			,0 	AS "MontoMercadoMonedaAdicionalActivo"
			,0 	AS "MontoPromedioCompraMonedaSistemaPasivo"
			,0 	AS "MontoPromedioCompraMonedaAdicionalPasivo"
			,0 	AS "MontoATasaCompraPasivo"
			,0 	AS "MontoMercadoMonedaSistemaPasivo"
			,0 	AS "MontoMercadoMonedaAdicionalPasivo"
		FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
		INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."FechaRespaldo" = :FechaHasta
		AND SUB."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."FechaLiquidacion" <= :FechaHasta
		AND CAR."IdTipoInstrumento" = 'EMP'
		GROUP BY 
		CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",SUB."IdClasificacionTipoInstrumento",
		SUB."IdTipoInstrumento",SUB."IdInstrumento",SUB."Nemotecnico",SUB."IdMonedaInstrumento",SUB."IdEmisor", CAR."INGPrecioPromedioCompra", SUB."INGPrecioMercadoActual"
		
UNION ALL
------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------	PRESTAMO PASIVO 
------------------------------------------------------------------------------------------------------------------------------------------	
 
		SELECT 	 
			 CAR."IdCliente"
			,CAR."IdPortafolio"
			,CAR."IdBoveda"
			,SUB."IdClasificacionTipoInstrumento"
			,SUB."IdTipoInstrumento"
			,SUB."IdInstrumento"
			,SUB."Nemotecnico"	
			,0												AS "DisponibleActivo"
			,0												AS "DisponiblePasivo"
			,0												AS "GarantiaActivo"
			,0												AS "GarantiaPasivo"	
			,0												AS "PrestamoActivo"
			,SUM(SUB."INGCantidad")							AS "PrestamoPasivo"		
			,0												AS "PlazoActivo"
			,0												AS "PlazoPasivo"			
			,SUB."IdMonedaInstrumento"
			,SUB."IdEmisor"
			--------------------------------------------------------------------------------------------
			,CAR."INGPrecioPromedioCompra" 						AS "PrecioPromedioCompra"
			,SUB."INGPrecioMercadoActual" 						AS "PrecioMercado"
			,0	AS "MontoPromedioCompraMonedaSistemaActivo"
			,0 	AS "MontoPromedioCompraMonedaAdicionalActivo"
			,0	AS "MontoATasaCompraActivo"
			,0	AS "MontoMercadoMonedaSistemaActivo"
			,0 	AS "MontoMercadoMonedaAdicionalActivo"
			,0 	AS "MontoPromedioCompraMonedaSistemaPasivo"
			,0 	AS "MontoPromedioCompraMonedaAdicionalPasivo"
			,0 	AS "MontoATasaCompraPasivo"
			,0 	AS "MontoMercadoMonedaSistemaPasivo"
			,0 	AS "MontoMercadoMonedaAdicionalPasivo"
		FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
		INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."FechaRespaldo" = :FechaHasta
		AND SUB."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."FechaLiquidacion" <= :FechaHasta
		AND CAR."IdTipoInstrumento" = 'PRT'
		GROUP BY 
		CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",SUB."IdClasificacionTipoInstrumento",
		SUB."IdTipoInstrumento",SUB."IdInstrumento",SUB."Nemotecnico",SUB."IdMonedaInstrumento",SUB."IdEmisor", CAR."INGPrecioPromedioCompra", SUB."INGPrecioMercadoActual" 
				
UNION ALL 		
------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------	PLAZO ACTIVO 
------------------------------------------------------------------------------------------------------------------------------------------	
		SELECT 	 
			 CAR."IdCliente"
			,CAR."IdPortafolio"
			,CAR."IdBoveda"
			,SUB."IdClasificacionTipoInstrumento"
			,SUB."IdTipoInstrumento"
			,SUB."IdInstrumento"
			,SUB."Nemotecnico"	
			,0												AS "DisponibleActivo"
			,0												AS "DisponiblePasivo"
			,0												AS "GarantiaActivo"
			,0												AS "GarantiaPasivo"
			,0												AS "PrestamoActivo"
			,0												AS "PrestamoPasivo"	
			,SUM(SUB."INGCantidad")							AS "PlazoActivo"
			,0												AS "PlazoPasivo"
			,SUB."IdMonedaInstrumento"
			,SUB."IdEmisor"
			--------------------------------------------------------------------------------------------
			,CAR."INGPrecioPromedioCompra" 						AS "PrecioPromedioCompra"
			,SUB."INGPrecioMercadoActual" 						AS "PrecioMercado"
			,0	AS "MontoPromedioCompraMonedaSistemaActivo"
			,0 	AS "MontoPromedioCompraMonedaAdicionalActivo"
			,0	AS "MontoATasaCompraActivo"
			,0	AS "MontoMercadoMonedaSistemaActivo"
			,0 	AS "MontoMercadoMonedaAdicionalActivo"
			,0 	AS "MontoPromedioCompraMonedaSistemaPasivo"
			,0 	AS "MontoPromedioCompraMonedaAdicionalPasivo"
			,0 	AS "MontoATasaCompraPasivo"
			,0 	AS "MontoMercadoMonedaSistemaPasivo"
			,0 	AS "MontoMercadoMonedaAdicionalPasivo"
		FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
		INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."FechaRespaldo" = :FechaHasta
		AND SUB."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."FechaLiquidacion" <= :FechaHasta
		AND CAR."IdTipoInstrumento" IN ('SIM','PAC','RRV','RRF')
		GROUP BY 
		CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",SUB."IdClasificacionTipoInstrumento",
		SUB."IdTipoInstrumento",SUB."IdInstrumento",SUB."Nemotecnico",SUB."IdMonedaInstrumento",SUB."IdEmisor", CAR."INGPrecioPromedioCompra", SUB."INGPrecioMercadoActual"
		
UNION ALL
------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------	PLAZO PASIVO 
------------------------------------------------------------------------------------------------------------------------------------------	
 
		SELECT 	 
			 CAR."IdCliente"
			,CAR."IdPortafolio"
			,CAR."IdBoveda"
			,SUB."IdClasificacionTipoInstrumento"
			,SUB."IdTipoInstrumento"
			,SUB."IdInstrumento"
			,SUB."Nemotecnico"	
			,0												AS "DisponibleActivo"
			,0												AS "DisponiblePasivo"
			,0												AS "GarantiaActivo"
			,0												AS "GarantiaPasivo"	
			,0												AS "PrestamoActivo"
			,0												AS "PrestamoPasivo"
			,0												AS "PlazoActivo"
			,SUM(SUB."INGCantidad")							AS "PlazoPasivo"
			,SUB."IdMonedaInstrumento"
			,SUB."IdEmisor"
			--------------------------------------------------------------------------------------------
			,CAR."INGPrecioPromedioCompra" 						AS "PrecioPromedioCompra"
			,SUB."INGPrecioMercadoActual" 						AS "PrecioMercado"
			,0	AS "MontoPromedioCompraMonedaSistemaActivo"
			,0 	AS "MontoPromedioCompraMonedaAdicionalActivo"
			,0	AS "MontoATasaCompraActivo"
			,0	AS "MontoMercadoMonedaSistemaActivo"
			,0 	AS "MontoMercadoMonedaAdicionalActivo"
			,0 	AS "MontoPromedioCompraMonedaSistemaPasivo"
			,0 	AS "MontoPromedioCompraMonedaAdicionalPasivo"
			,0 	AS "MontoATasaCompraPasivo"
			,0 	AS "MontoMercadoMonedaSistemaPasivo"
			,0 	AS "MontoMercadoMonedaAdicionalPasivo"
		FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
		INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."FechaRespaldo" = :FechaHasta
		AND SUB."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."FechaLiquidacion" <= :FechaHasta
		AND CAR."IdTipoInstrumento" IN ('SIM','PAC','RRV','RRF')
		GROUP BY 
		CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",SUB."IdClasificacionTipoInstrumento",
		SUB."IdTipoInstrumento",SUB."IdInstrumento",SUB."Nemotecnico",SUB."IdMonedaInstrumento",SUB."IdEmisor", CAR."INGPrecioPromedioCompra", SUB."INGPrecioMercadoActual" 		
		)

	SELECT 		
	
	 		 "IdCliente"
			,"IdPortafolio"
			,"IdBoveda"
			,"IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento"
			,"IdInstrumento"
			,"Nemotecnico"	
			,TO_DECIMAL(IFNULL(SUM("DisponibleActivo"),TO_DECIMAL(0,26,8)),26,8)	AS "DisponibleActivo"
			,TO_DECIMAL(IFNULL(SUM("DisponiblePasivo"),TO_DECIMAL(0,26,8)),26,8)	AS "DisponiblePasivo" 
			,TO_DECIMAL(IFNULL(SUM("GarantiaActivo"),TO_DECIMAL(0,26,8)),26,8)		AS "GarantiaActivo"
			,TO_DECIMAL(IFNULL(SUM("GarantiaPasivo"),TO_DECIMAL(0,26,8)),26,8)		AS "GarantiaPasivo"					
			,TO_DECIMAL(IFNULL(SUM("PrestamoActivo"),TO_DECIMAL(0,26,8)),26,8)		AS "PrestamoActivo"
			,TO_DECIMAL(IFNULL(SUM("PrestamoPasivo"),TO_DECIMAL(0,26,8)),26,8)		AS "PrestamoPasivo"	
			,TO_DECIMAL(IFNULL(SUM("PlazoActivo"),TO_DECIMAL(0,26,8)),26,8)			AS "PlazoActivo"
			,TO_DECIMAL(IFNULL(SUM("PlazoPasivo"),TO_DECIMAL(0,26,8)),26,8)			AS "PlazoPasivo"
			,"IdMonedaInstrumento"
			,"IdEmisor"
			--------------------------------------------------------------------------------------------
			,"PrecioPromedioCompra"
			,"PrecioMercado"
			,TO_DECIMAL(SUM("MontoPromedioCompraMonedaSistemaActivo"),26,8) AS "MontoPromedioCompraMonedaSistemaActivo"
			,TO_DECIMAL(SUM("MontoPromedioCompraMonedaAdicionalActivo"),26,8) AS "MontoPromedioCompraMonedaAdicionalActivo"
			,TO_DECIMAL(SUM("MontoATasaCompraActivo"),26,8) AS "MontoATasaCompraActivo"
			,TO_DECIMAL(SUM("MontoMercadoMonedaSistemaActivo"),26,8) AS "MontoMercadoMonedaSistemaActivo"
			,TO_DECIMAL(SUM("MontoMercadoMonedaAdicionalActivo"),26,8) AS "MontoMercadoMonedaAdicionalActivo"
			,TO_DECIMAL(SUM("MontoPromedioCompraMonedaSistemaPasivo"),26,8) AS "MontoPromedioCompraMonedaSistemaPasivo"
			,TO_DECIMAL(SUM("MontoPromedioCompraMonedaAdicionalPasivo"),26,8) AS "MontoPromedioCompraMonedaAdicionalPasivo"
			,TO_DECIMAL(SUM("MontoATasaCompraPasivo"),26,8) AS "MontoATasaCompraPasivo"
			,TO_DECIMAL(SUM("MontoMercadoMonedaSistemaPasivo"),26,8) AS "MontoMercadoMonedaSistemaPasivo"
			,TO_DECIMAL(SUM("MontoMercadoMonedaAdicionalPasivo"),26,8) AS "MontoMercadoMonedaAdicionalPasivo"
	FROM Cartera
	GROUP BY 
		"IdCliente",
		"IdPortafolio",
		"IdBoveda",
		"IdClasificacionTipoInstrumento",
		"IdTipoInstrumento",
		"IdInstrumento",
		"Nemotecnico",
		"IdMonedaInstrumento",
		"IdEmisor", 
		"PrecioPromedioCompra",
		"PrecioMercado"
	ORDER BY 	"IdCliente"	,"IdPortafolio","IdInstrumento"