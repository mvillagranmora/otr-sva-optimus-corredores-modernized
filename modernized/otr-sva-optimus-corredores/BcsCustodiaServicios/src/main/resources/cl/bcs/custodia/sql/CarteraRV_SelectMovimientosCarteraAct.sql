WITH 
	Cartera AS
	(
------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------	DISPONIBLE ACTIVO 
------------------------------------------------------------------------------------------------------------------------------------------	
		SELECT 	 
			 CAR."IdCliente"
			,CAR."IdPortafolio"
--			,CAR."FolioCartera"
--			,CAR."INGFechaOperacion" 				AS "FechaOperacion"
--			,CAR."INGFechaLiquidacion" 				AS "FechaLiquidacion"
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
			,CAR."INGPrecioPromedioCompra" 					AS "PrecioPromedioCompra"
			,CAR."INGMontoPrecioPromedioCompra" 			AS "MontoPromedioCompraMonedaSistema"
			,CAR."INGMontoPrecioPromedioMonedaAdicional" 	AS "MontoPromedioCompraMonedaAdicional"
			,CAR."INGMontoATasaCompraActual" 				AS "MontoATasaCompra"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR

		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."FechaOperacion" <= :FechaHasta
	  	AND CAR."EGRCantidad" = 0
		AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		GROUP BY 
		"IdCliente","IdPortafolio","FolioCartera","INGFechaOperacion","INGFechaLiquidacion","IdBoveda","IdClasificacionTipoInstrumento",
		"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","IdEmisor", CAR."INGPrecioPromedioCompra",CAR."INGMontoPrecioPromedioCompra",
		CAR."INGMontoPrecioPromedioMonedaAdicional",CAR."INGMontoATasaCompraActual"
		
UNION ALL
------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------	DISPONIBLE PASIVO 
------------------------------------------------------------------------------------------------------------------------------------------	
 
		SELECT 	 
			 CAR."IdCliente"
			,CAR."IdPortafolio"
--			,CAR."FolioCartera"
--			,CAR."INGFechaOperacion" 				AS "FechaOperacion"
--			,CAR."INGFechaLiquidacion" 				AS "FechaLiquidacion"
			,CAR."IdBoveda"
			,CAR."IdClasificacionTipoInstrumento"
			,CAR."IdTipoInstrumento"
			,CAR."IdInstrumento"
			,CAR."Nemotecnico"	
			,0												AS "DisponibleActivo"
			,150											AS "DisponiblePasivo"
			,0												AS "GarantiaActivo"
			,0												AS "GarantiaPasivo"		
			,0												AS "PrestamoActivo"
			,0												AS "PrestamoPasivo"	
			,0												AS "PlazoActivo"
			,0												AS "PlazoPasivo"			
			
			,CAR."IdMonedaInstrumento"
			,CAR."IdEmisor"
			--------------------------------------------------------------------------------------------
			,CAR."INGPrecioPromedioCompra" 					AS "PrecioPromedioCompra"
			,CAR."INGMontoPrecioPromedioCompra" 			AS "MontoPromedioCompraMonedaSistema"
			,CAR."INGMontoPrecioPromedioMonedaAdicional" 	AS "MontoPromedioCompraMonedaAdicional"
			,CAR."INGMontoATasaCompraActual" 				AS "MontoATasaCompra"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR

		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."FechaOperacion" <= :FechaHasta
	  	AND CAR."EGRCantidad" = 0
		AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		GROUP BY 
		"IdCliente","IdPortafolio","FolioCartera","INGFechaOperacion","INGFechaLiquidacion","IdBoveda","IdClasificacionTipoInstrumento",
		"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","IdEmisor", CAR."INGPrecioPromedioCompra",CAR."INGMontoPrecioPromedioCompra",
		CAR."INGMontoPrecioPromedioMonedaAdicional",CAR."INGMontoATasaCompraActual" 
UNION ALL 		
------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------	GARANTIA ACTIVO 
------------------------------------------------------------------------------------------------------------------------------------------	
		SELECT 	 
			 CAR."IdCliente"
			,CAR."IdPortafolio"
--			,CAR."FolioCartera"
--			,CAR."INGFechaOperacion" 				AS "FechaOperacion"
--			,CAR."INGFechaLiquidacion" 				AS "FechaLiquidacion"
			,CAR."IdBoveda"
			,CAR."IdClasificacionTipoInstrumento"
			,CAR."IdTipoInstrumento"
			,CAR."IdInstrumento"
			,CAR."Nemotecnico"	
			,0												AS "DisponibleActivo"
			,0												AS "DisponiblePasivo"
			,300											AS "GarantiaActivo"
			,0												AS "GarantiaPasivo"
			,0												AS "PrestamoActivo"
			,0												AS "PrestamoPasivo"
			,0												AS "PlazoActivo"
			,0												AS "PlazoPasivo"			
			,CAR."IdMonedaInstrumento"
			,CAR."IdEmisor"
			--------------------------------------------------------------------------------------------
			,CAR."INGPrecioPromedioCompra" 					AS "PrecioPromedioCompra"
			,CAR."INGMontoPrecioPromedioCompra" 			AS "MontoPromedioCompraMonedaSistema"
			,CAR."INGMontoPrecioPromedioMonedaAdicional" 	AS "MontoPromedioCompraMonedaAdicional"
			,CAR."INGMontoATasaCompraActual" 				AS "MontoATasaCompra"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR

		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."FechaOperacion" <= :FechaHasta
	  	AND CAR."EGRCantidad" = 0
		AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		GROUP BY 
		"IdCliente","IdPortafolio","FolioCartera","INGFechaOperacion","INGFechaLiquidacion","IdBoveda","IdClasificacionTipoInstrumento",
		"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","IdEmisor", CAR."INGPrecioPromedioCompra",CAR."INGMontoPrecioPromedioCompra",
		CAR."INGMontoPrecioPromedioMonedaAdicional",CAR."INGMontoATasaCompraActual"
		
UNION ALL
------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------	GARANTIA PASIVO 
------------------------------------------------------------------------------------------------------------------------------------------	
 
		SELECT 	 
			 CAR."IdCliente"
			,CAR."IdPortafolio"
--			,CAR."FolioCartera"
--			,CAR."INGFechaOperacion" 				AS "FechaOperacion"
--			,CAR."INGFechaLiquidacion" 				AS "FechaLiquidacion"
			,CAR."IdBoveda"
			,CAR."IdClasificacionTipoInstrumento"
			,CAR."IdTipoInstrumento"
			,CAR."IdInstrumento"
			,CAR."Nemotecnico"	
			,0												AS "DisponibleActivo"
			,0												AS "DisponiblePasivo"
			,0												AS "GarantiaActivo"
			,200											AS "GarantiaPasivo"	
			,0												AS "PrestamoActivo"
			,0												AS "PrestamoPasivo"
			,0												AS "PlazoActivo"
			,0												AS "PlazoPasivo"			
			
			,CAR."IdMonedaInstrumento"
			,CAR."IdEmisor"
			--------------------------------------------------------------------------------------------
			,CAR."INGPrecioPromedioCompra" 					AS "PrecioPromedioCompra"
			,CAR."INGMontoPrecioPromedioCompra" 			AS "MontoPromedioCompraMonedaSistema"
			,CAR."INGMontoPrecioPromedioMonedaAdicional" 	AS "MontoPromedioCompraMonedaAdicional"
			,CAR."INGMontoATasaCompraActual" 				AS "MontoATasaCompra"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR

		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."FechaOperacion" <= :FechaHasta
	  	AND CAR."EGRCantidad" = 0
		AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		GROUP BY 
		"IdCliente","IdPortafolio","FolioCartera","INGFechaOperacion","INGFechaLiquidacion","IdBoveda","IdClasificacionTipoInstrumento",
		"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","IdEmisor", CAR."INGPrecioPromedioCompra",CAR."INGMontoPrecioPromedioCompra",
		CAR."INGMontoPrecioPromedioMonedaAdicional",CAR."INGMontoATasaCompraActual" 
		
		
UNION ALL 		
------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------	PRESTAMO ACTIVO 
------------------------------------------------------------------------------------------------------------------------------------------	
		SELECT 	 
			 CAR."IdCliente"
			,CAR."IdPortafolio"
--			,CAR."FolioCartera"
--			,CAR."INGFechaOperacion" 				AS "FechaOperacion"
--			,CAR."INGFechaLiquidacion" 				AS "FechaLiquidacion"
			,CAR."IdBoveda"
			,CAR."IdClasificacionTipoInstrumento"
			,CAR."IdTipoInstrumento"
			,CAR."IdInstrumento"
			,CAR."Nemotecnico"	
			,0												AS "DisponibleActivo"
			,0												AS "DisponiblePasivo"
			,0												AS "GarantiaActivo"
			,0												AS "GarantiaPasivo"
			,500											AS "PrestamoActivo"
			,0												AS "PrestamoPasivo"		
			,0												AS "PlazoActivo"
			,0												AS "PlazoPasivo"			
			
			,CAR."IdMonedaInstrumento"
			,CAR."IdEmisor"
			--------------------------------------------------------------------------------------------
			,CAR."INGPrecioPromedioCompra" 					AS "PrecioPromedioCompra"
			,CAR."INGMontoPrecioPromedioCompra" 			AS "MontoPromedioCompraMonedaSistema"
			,CAR."INGMontoPrecioPromedioMonedaAdicional" 	AS "MontoPromedioCompraMonedaAdicional"
			,CAR."INGMontoATasaCompraActual" 				AS "MontoATasaCompra"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR

		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."FechaOperacion" <= :FechaHasta
	  	AND CAR."EGRCantidad" = 0
		AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		GROUP BY 
		"IdCliente","IdPortafolio","FolioCartera","INGFechaOperacion","INGFechaLiquidacion","IdBoveda","IdClasificacionTipoInstrumento",
		"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","IdEmisor", CAR."INGPrecioPromedioCompra",CAR."INGMontoPrecioPromedioCompra",
		CAR."INGMontoPrecioPromedioMonedaAdicional",CAR."INGMontoATasaCompraActual"
		
UNION ALL
------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------	PRESTAMO PASIVO 
------------------------------------------------------------------------------------------------------------------------------------------	
 
		SELECT 	 
			 CAR."IdCliente"
			,CAR."IdPortafolio"
--			,CAR."FolioCartera"
--			,CAR."INGFechaOperacion" 				AS "FechaOperacion"
--			,CAR."INGFechaLiquidacion" 				AS "FechaLiquidacion"
			,CAR."IdBoveda"
			,CAR."IdClasificacionTipoInstrumento"
			,CAR."IdTipoInstrumento"
			,CAR."IdInstrumento"
			,CAR."Nemotecnico"	
			,0												AS "DisponibleActivo"
			,0												AS "DisponiblePasivo"
			,0												AS "GarantiaActivo"
			,0												AS "GarantiaPasivo"	
			,0												AS "PrestamoActivo"
			,200											AS "PrestamoPasivo"		
			,0												AS "PlazoActivo"
			,0												AS "PlazoPasivo"			
			,CAR."IdMonedaInstrumento"
			,CAR."IdEmisor"
			--------------------------------------------------------------------------------------------
			,CAR."INGPrecioPromedioCompra" 					AS "PrecioPromedioCompra"
			,CAR."INGMontoPrecioPromedioCompra" 			AS "MontoPromedioCompraMonedaSistema"
			,CAR."INGMontoPrecioPromedioMonedaAdicional" 	AS "MontoPromedioCompraMonedaAdicional"
			,CAR."INGMontoATasaCompraActual" 				AS "MontoATasaCompra"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR

		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."FechaOperacion" <= :FechaHasta
	  	AND CAR."EGRCantidad" = 0
		AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		GROUP BY 
		"IdCliente","IdPortafolio","FolioCartera","INGFechaOperacion","INGFechaLiquidacion","IdBoveda","IdClasificacionTipoInstrumento",
		"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","IdEmisor", CAR."INGPrecioPromedioCompra",CAR."INGMontoPrecioPromedioCompra",
		CAR."INGMontoPrecioPromedioMonedaAdicional",CAR."INGMontoATasaCompraActual" 
				
UNION ALL 		
------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------	PLAZO ACTIVO 
------------------------------------------------------------------------------------------------------------------------------------------	
		SELECT 	 
			 CAR."IdCliente"
			,CAR."IdPortafolio"
--			,CAR."FolioCartera"
--			,CAR."INGFechaOperacion" 				AS "FechaOperacion"
--			,CAR."INGFechaLiquidacion" 				AS "FechaLiquidacion"
			,CAR."IdBoveda"
			,CAR."IdClasificacionTipoInstrumento"
			,CAR."IdTipoInstrumento"
			,CAR."IdInstrumento"
			,CAR."Nemotecnico"	
			,0												AS "DisponibleActivo"
			,0												AS "DisponiblePasivo"
			,0												AS "GarantiaActivo"
			,0												AS "GarantiaPasivo"
			,0												AS "PrestamoActivo"
			,0												AS "PrestamoPasivo"	
			,230											AS "PlazoActivo"
			,0												AS "PlazoPasivo"			
			
			,CAR."IdMonedaInstrumento"
			,CAR."IdEmisor"
			--------------------------------------------------------------------------------------------
			,CAR."INGPrecioPromedioCompra" 					AS "PrecioPromedioCompra"
			,CAR."INGMontoPrecioPromedioCompra" 			AS "MontoPromedioCompraMonedaSistema"
			,CAR."INGMontoPrecioPromedioMonedaAdicional" 	AS "MontoPromedioCompraMonedaAdicional"
			,CAR."INGMontoATasaCompraActual" 				AS "MontoATasaCompra"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR

		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."FechaOperacion" <= :FechaHasta
	  	AND CAR."EGRCantidad" = 0
		AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		GROUP BY 
		"IdCliente","IdPortafolio","FolioCartera","INGFechaOperacion","INGFechaLiquidacion","IdBoveda","IdClasificacionTipoInstrumento",
		"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","IdEmisor", CAR."INGPrecioPromedioCompra",CAR."INGMontoPrecioPromedioCompra",
		CAR."INGMontoPrecioPromedioMonedaAdicional",CAR."INGMontoATasaCompraActual"
		
UNION ALL
------------------------------------------------------------------------------------------------------------------------------------------
----------------------------------	PLAZO PASIVO 
------------------------------------------------------------------------------------------------------------------------------------------	
 
		SELECT 	 
			 CAR."IdCliente"
			,CAR."IdPortafolio"
--			,CAR."FolioCartera"
--			,CAR."INGFechaOperacion" 				AS "FechaOperacion"
--			,CAR."INGFechaLiquidacion" 				AS "FechaLiquidacion"
			,CAR."IdBoveda"
			,CAR."IdClasificacionTipoInstrumento"
			,CAR."IdTipoInstrumento"
			,CAR."IdInstrumento"
			,CAR."Nemotecnico"	
			,0												AS "DisponibleActivo"
			,0												AS "DisponiblePasivo"
			,0												AS "GarantiaActivo"
			,0												AS "GarantiaPasivo"	
			,0												AS "PrestamoActivo"
			,0												AS "PrestamoPasivo"
			,0												AS "PlazoActivo"
			,50												AS "PlazoPasivo"			
			
			,CAR."IdMonedaInstrumento"
			,CAR."IdEmisor"
			--------------------------------------------------------------------------------------------
			,CAR."INGPrecioPromedioCompra" 					AS "PrecioPromedioCompra"
			,CAR."INGMontoPrecioPromedioCompra" 			AS "MontoPromedioCompraMonedaSistema"
			,CAR."INGMontoPrecioPromedioMonedaAdicional" 	AS "MontoPromedioCompraMonedaAdicional"
			,CAR."INGMontoATasaCompraActual" 				AS "MontoATasaCompra"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR

		WHERE 
		CAR."IdPortafolio" IN (:IdPortafolio)
		AND CAR."IdClasificacionTipoInstrumento" = 'RV'
	  	AND CAR."IdEmpresa" = :IdEmpresa
	  	AND CAR."IdEstado" = 'VIG'
	  	AND CAR."FechaOperacion" <= :FechaHasta
	  	AND CAR."EGRCantidad" = 0
		AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		GROUP BY 
		"IdCliente","IdPortafolio","FolioCartera","INGFechaOperacion","INGFechaLiquidacion","IdBoveda","IdClasificacionTipoInstrumento",
		"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","IdEmisor", CAR."INGPrecioPromedioCompra",CAR."INGMontoPrecioPromedioCompra",
		CAR."INGMontoPrecioPromedioMonedaAdicional",CAR."INGMontoATasaCompraActual" 		
		)

	SELECT 		
	
	 		 "IdCliente"
			,"IdPortafolio"
			,"IdBoveda"
			,"IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento"
			,"IdInstrumento"
			,"Nemotecnico"	
			,IFNULL(SUM("DisponibleActivo"),TO_DECIMAL(0,26,8))	AS "DisponibleActivo"
			,IFNULL(SUM("DisponiblePasivo"),TO_DECIMAL(0,26,8))	AS "DisponiblePasivo" 
			,IFNULL(SUM("GarantiaActivo"),TO_DECIMAL(0,26,8))	AS "GarantiaActivo"
			,IFNULL(SUM("GarantiaPasivo"),TO_DECIMAL(0,26,8))	AS "GarantiaPasivo"					
			,IFNULL(SUM("PrestamoActivo"),TO_DECIMAL(0,26,8))	AS "PrestamoActivo"
			,IFNULL(SUM("PrestamoPasivo"),TO_DECIMAL(0,26,8))	AS "PrestamoPasivo"	
			,IFNULL(SUM("PlazoActivo"),TO_DECIMAL(0,26,8))		AS "PlazoActivo"
			,IFNULL(SUM("PlazoPasivo"),TO_DECIMAL(0,26,8))		AS "PlazoPasivo"			
			
			
			,"IdMonedaInstrumento"
			,"IdEmisor"
			--------------------------------------------------------------------------------------------
			,"PrecioPromedioCompra"
			,"MontoPromedioCompraMonedaSistema"
			,"MontoPromedioCompraMonedaAdicional"
			,"MontoATasaCompra"
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
		"MontoPromedioCompraMonedaSistema",
		"MontoPromedioCompraMonedaAdicional",
		"MontoATasaCompra"
		
	ORDER BY 	"IdCliente"	,"IdPortafolio","IdInstrumento"