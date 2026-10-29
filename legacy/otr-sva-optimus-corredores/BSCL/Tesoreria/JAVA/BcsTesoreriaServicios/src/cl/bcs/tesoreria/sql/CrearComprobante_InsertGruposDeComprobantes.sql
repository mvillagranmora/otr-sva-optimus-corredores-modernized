
-- Relaciona los movimientos con la configuracion de agrupacion, para generar los grupos de comprobantes
INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_GruposDeComprobantes"
(
	"Grupo","IdComprobante","NumeroReferencia","TipoComprobante","IdPersonaTitularPago","IdPortafolioTitularPago",
	"IdMoneda","IdCaja","FechaLiquidacion","SecuenciaAgrupacion","MueveCuentaInversion","NeteaComprobante","IdTipoMovimiento",
	"AgrupaComprobante","IdInternoTes","IdConcepto","IdEmpleadoDigitador","IdSucursalEmpleadoDigitador",
	"IdTipoComprobanteOrigen","IdTipoProducto","IdTipoOperacion","IdBolsa","IdCondicionDeLiquidacion", "Glosa"
)
SELECT  RANK() over 
		(order by	"NumeroReferencia",
					"TipoComprobante",
					"IdPersonaTitularPago",
					"IdPortafolioTitularPago",
					"FechaLiquidacion",
					"IdMoneda", 
	                "IdCaja", 					                
	                "SecuenciaAgrupacion",
	                "MueveCuentaInversion",
	                "NeteaComprobante",
	                "IdTipoMovimiento",
	                "AgrupaComprobante",	
	                "IdInternoTes",
	                "IdConcepto",
	                "IdEmpleadoDigitador",
	                "IdSucursalEmpleadoDigitador",
	                "IdTipoComprobanteOrigen",
	                "IdTipoProducto",
	                "IdTipoOperacion",
	                "IdBolsa",
	                "IdCondicionDeLiquidacion"
		) as "Grupo",
		"BSCL.Tesoreria.BD.Secuencias::Comprobante".NEXTVAL AS "IdComprobante",
		"NumeroReferencia",
		"TipoComprobante",
		"IdPersonaTitularPago",
		"IdPortafolioTitularPago",
		"IdMoneda", 
	    "IdCaja", 
	    "FechaLiquidacion",
	    "SecuenciaAgrupacion",
	    "MueveCuentaInversion",
	    "NeteaComprobante",
		"IdTipoMovimiento",
		"AgrupaComprobante",
		"IdInternoTes",
		"IdConcepto",
		"IdEmpleadoDigitador",
		"IdSucursalEmpleadoDigitador",
		"IdTipoComprobanteOrigen",
		"IdTipoProducto",
		"IdTipoOperacion",
		"IdBolsa",
		COALESCE ("IdCondicionDeLiquidacion",'') AS "IdCondicionDeLiquidacion",
		CASE 
			WHEN COUNT(DISTINCT NULLIF("Glosa", '')) = 1 THEN MAX(NULLIF("Glosa", ''))    
			WHEN COUNT(DISTINCT NULLIF("Glosa", '')) > 1 THEN ''   WHEN COUNT("Glosa") = 1 THEN MAX("Glosa")
			ELSE ''
		END AS "Glosa"
  FROM (SELECT  IngrEgr."NumeroReferencia",
  				IngrEgr."TipoComprobante",
				CASE WHEN LENGTH(IngrEgr."TipoComprobante") = 0 THEN IngrEgr."IdPersonaTitularPago" END AS "IdPersonaTitularPago",
				CASE WHEN LENGTH(IngrEgr."TipoComprobante") = 0 THEN IngrEgr."IdPortafolioTitularPago" END AS "IdPortafolioTitularPago",       
                IngrEgr."FechaLiquidacion",
                IngrEgr."IdMoneda",			                                    
                IngrEgr."SecuenciaAgrupacion",
                IngrEgr."MueveCuentaInversion",              
                CASE WHEN IngrEgr."IdCajaComprobante" = 0 THEN IngrEgr."IdCaja" End  as "IdCaja",
                CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0) 											THEN IngrEgr."NeteaComprobante" 			END "NeteaComprobante",
				CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND IngrEgr."NeteaComprobante" = 'N') 		THEN IngrEgr."IdTipoMovimiento" 			END "IdTipoMovimiento",
				CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND IngrEgr."AgrupaComprobante" = 'N') 		THEN IngrEgr."AgrupaComprobante" 			END "AgrupaComprobante",
				CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND IngrEgr."AgrupaComprobante" = 'N') 		THEN IngrEgr."IdIngresoEgreso" 				END "IdInternoTes",												
				CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND :SEPARAPORCONCEPTO  = 'S') 		THEN IngrEgr."IdConcepto" 					END "IdConcepto",
				CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND :SEPARAPORAGENTE = 'S') 			THEN IngrEgr."IdEmpleadoDigitador" 			END "IdEmpleadoDigitador",
				CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND :SEPARAPORSUCURSAL = 'S') 		THEN IngrEgr."IdSucursalEmpleadoDigitador"	END "IdSucursalEmpleadoDigitador",							
		 		CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND :SEPARAPORTIPOCOMPROBANTE = 'S') THEN IngrEgr."IdTipoComprobanteOrigen"	 	END "IdTipoComprobanteOrigen",
		 		CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND :SEPARAPORTIPOPRODUCTO = 'S') 	THEN IngrEgr."IdTipoProducto" 				END "IdTipoProducto",
		 		CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND :SEPARAPORTIPOOPERACION = 'S') 	THEN IngrEgr."IdTipoOperacion" 				END "IdTipoOperacion",
		 		CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND :SEPARAPORBOLSA = 'S') 			THEN IngrEgr."IdBolsa" 						END "IdBolsa",
		 		CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND :SEPARAPORCONDICIONLIQUIDACION = 'S') 			THEN IngrEgr."IdCondicionDeLiquidacion" 						END "IdCondicionDeLiquidacion",
		 		COALESCE(IngrEgr."Glosa", '') AS "Glosa"
		  FROM "BSCL.Tesoreria.BD::GBL.IngresoEgresoGenerarComprobante_Proceso" AS IngrEgr
  	   )
 GROUP BY "NumeroReferencia","TipoComprobante","IdPersonaTitularPago","IdPortafolioTitularPago","IdMoneda","IdCaja","FechaLiquidacion",
 	"SecuenciaAgrupacion","MueveCuentaInversion","NeteaComprobante","IdConcepto","IdTipoMovimiento","AgrupaComprobante","IdInternoTes",
 	"IdEmpleadoDigitador","IdSucursalEmpleadoDigitador","IdTipoComprobanteOrigen","IdTipoProducto","IdTipoOperacion","IdBolsa","IdCondicionDeLiquidacion";
		 
		 