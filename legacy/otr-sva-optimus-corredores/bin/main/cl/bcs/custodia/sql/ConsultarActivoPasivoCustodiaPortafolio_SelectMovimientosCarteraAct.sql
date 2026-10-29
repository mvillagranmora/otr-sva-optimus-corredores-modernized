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
			,SUM(CAR."INGMontoPrecioPromedioCompra") 			AS "MontoPromedioCompraMonedaSistemaActivo"
			,SUM(CAR."INGMontoPrecioPromedioMonedaAdicional") 	AS "MontoPromedioCompraMonedaAdicionalActivo"
			,SUM(CAR."INGMontoATasaCompraActual") 				AS "MontoATasaCompraActivo"
			,0 													AS "MontoPromedioCompraMonedaSistemaPasivo"
			,0 													AS "MontoPromedioCompraMonedaAdicionalPasivo"
			,0 													AS "MontoATasaCompraPasivo"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."FechaLiquidacion" <= :FechaHasta
	  	AND CAR."EGRCantidad" = 0
		AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		GROUP BY 
		CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",CAR."IdClasificacionTipoInstrumento",
		CAR."IdTipoInstrumento",CAR."IdInstrumento",CAR."Nemotecnico",CAR."IdMonedaInstrumento",CAR."IdEmisor", CAR."INGPrecioPromedioCompra"
		
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
			,0 													AS "MontoPromedioCompraMonedaSistemaActivo"
			,0 													AS "MontoPromedioCompraMonedaAdicionalActivo"
			,0 													AS "MontoATasaCompraActivo"
			,SUM(CAR."MontoCompra") 								AS "MontoPromedioCompraMonedaSistemaPasivo"
			,SUM(CAR."MontoCompraMonedaInstrumento") 				AS "MontoPromedioCompraMonedaAdicionalPasivo"
			,SUM(CAR."MontoCompra")									AS "MontoATasaCompraPasivo"
		FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS CAR
		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'PEN'
	  	AND CAR."FechaOperacion" <= :FechaHasta
		AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		AND CAR."EsVentaDescubierta" = 'S'
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
			,0 													AS "MontoPromedioCompraMonedaSistemaActivo"
			,0 													AS "MontoPromedioCompraMonedaAdicionalActivo"
			,0 													AS "MontoATasaCompraActivo"
			,0 													AS "MontoPromedioCompraMonedaSistemaPasivo"
			,0 													AS "MontoPromedioCompraMonedaAdicionalPasivo"
			,0 													AS "MontoATasaCompraPasivo"
		FROM "BSCL.Custodia.BD::GTA.Garantia" GAR
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON GAR."FolioCarteraOrigen" = CAR."FolioCartera"
		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND GAR."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND GAR."IdEstado" = 'VIG'
	  	AND GAR."FechaOperacion" <= :FechaHasta
		GROUP BY 
		CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",GAR."IdClasificacionTipoInstrumento",
		GAR."IdTipoInstrumento",GAR."IdInstrumento",GAR."Nemotecnico",GAR."IdMonedaInstrumento",GAR."IdEmisor", CAR."INGPrecioPromedioCompra"		
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
			,0 													AS "MontoPromedioCompraMonedaSistemaActivo"
			,0 													AS "MontoPromedioCompraMonedaAdicionalActivo"
			,0 													AS "MontoATasaCompraActivo"
			,0 													AS "MontoPromedioCompraMonedaSistemaPasivo"
			,0 													AS "MontoPromedioCompraMonedaAdicionalPasivo"
			,0 													AS "MontoATasaCompraPasivo"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND SUB."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	  	AND SUB."IdEstado" = 'VIG'
	  	AND CAR."FechaLiquidacion" <= :FechaHasta
		AND CAR."IdTipoInstrumento" = 'EMP'
		GROUP BY 
		CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",SUB."IdClasificacionTipoInstrumento",
		SUB."IdTipoInstrumento",SUB."IdInstrumento",SUB."Nemotecnico",SUB."IdMonedaInstrumento",SUB."IdEmisor", CAR."INGPrecioPromedioCompra"
		
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
			,CAR."INGPrecioPromedioCompra" 					AS "PrecioPromedioCompra"
			,0 													AS "MontoPromedioCompraMonedaSistemaActivo"
			,0 													AS "MontoPromedioCompraMonedaAdicionalActivo"
			,0 													AS "MontoATasaCompraActivo"
			,0 													AS "MontoPromedioCompraMonedaSistemaPasivo"
			,0 													AS "MontoPromedioCompraMonedaAdicionalPasivo"
			,0 													AS "MontoATasaCompraPasivo"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND SUB."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	  	AND SUB."IdEstado" = 'VIG'
	  	AND CAR."FechaLiquidacion" <= :FechaHasta
		AND CAR."IdTipoInstrumento" = 'PRT'
		GROUP BY 
		CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",SUB."IdClasificacionTipoInstrumento",
		SUB."IdTipoInstrumento",SUB."IdInstrumento",SUB."Nemotecnico",SUB."IdMonedaInstrumento",SUB."IdEmisor", CAR."INGPrecioPromedioCompra"
				
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
			,CAR."INGPrecioPromedioCompra" 					AS "PrecioPromedioCompra"
			,0 													AS "MontoPromedioCompraMonedaSistemaActivo"
			,0 													AS "MontoPromedioCompraMonedaAdicionalActivo"
			,0 													AS "MontoATasaCompraActivo"
			,0 													AS "MontoPromedioCompraMonedaSistemaPasivo"
			,0 													AS "MontoPromedioCompraMonedaAdicionalPasivo"
			,0 													AS "MontoATasaCompraPasivo"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND SUB."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	  	AND SUB."IdEstado" = 'VIG'
	  	AND CAR."FechaLiquidacion" <= :FechaHasta
		AND CAR."IdTipoInstrumento" IN ('SIM','PAC','RRV','RRF')
		GROUP BY 
		CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",SUB."IdClasificacionTipoInstrumento",
		SUB."IdTipoInstrumento",SUB."IdInstrumento",SUB."Nemotecnico",SUB."IdMonedaInstrumento",SUB."IdEmisor", CAR."INGPrecioPromedioCompra"
		
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
			,CAR."INGPrecioPromedioCompra" 					AS "PrecioPromedioCompra"
			,0 													AS "MontoPromedioCompraMonedaSistemaActivo"
			,0 													AS "MontoPromedioCompraMonedaAdicionalActivo"
			,0 													AS "MontoATasaCompraActivo"
			,0 													AS "MontoPromedioCompraMonedaSistemaPasivo"
			,0 													AS "MontoPromedioCompraMonedaAdicionalPasivo"
			,0 													AS "MontoATasaCompraPasivo"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND SUB."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	  	AND SUB."IdEstado" = 'VIG'
	  	AND CAR."FechaLiquidacion" <= :FechaHasta
		AND CAR."IdTipoInstrumento" IN ('SIM','PAC','RRV','RRF')
		GROUP BY 
		CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",SUB."IdClasificacionTipoInstrumento",
		SUB."IdTipoInstrumento",SUB."IdInstrumento",SUB."Nemotecnico",SUB."IdMonedaInstrumento",SUB."IdEmisor", CAR."INGPrecioPromedioCompra"		
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
			,TO_DECIMAL(SUM("MontoPromedioCompraMonedaSistemaActivo"),26,8) AS "MontoPromedioCompraMonedaSistemaActivo"
			,TO_DECIMAL(SUM("MontoPromedioCompraMonedaAdicionalActivo"),26,8) AS "MontoPromedioCompraMonedaAdicionalActivo"
			,TO_DECIMAL(SUM("MontoATasaCompraActivo"),26,8) AS "MontoATasaCompraActivo"
			,TO_DECIMAL(SUM("MontoPromedioCompraMonedaSistemaPasivo"),26,8) AS "MontoPromedioCompraMonedaSistemaPasivo"
			,TO_DECIMAL(SUM("MontoPromedioCompraMonedaAdicionalPasivo"),26,8) AS "MontoPromedioCompraMonedaAdicionalPasivo"
			,TO_DECIMAL(SUM("MontoATasaCompraPasivo"),26,8) AS "MontoATasaCompraPasivo"
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
		"PrecioPromedioCompra"
	ORDER BY 	"IdCliente"	,"IdPortafolio","IdInstrumento"