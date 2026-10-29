SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContrato"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContrato" AS
SELECT  DISTINCT
		FACT."IdComprobante"
   	   ,FACT."FechaComprobante"
	   ,FACT."IdCliente"
	   ,FACT."NombreLargoCliente"						AS "NombreCliente"
	   ,FACT."DocumentoIdentidad"
	   ,FACT."IdPortafolio"
	   ,POR."CodigoPortafolio"
	   ,FACT."IdMoneda"
	   ,FACT."IdClasificacionTipoInstrumento"
	   ,FACT."IdCondicionLiquidacion"
	   ,FACT."MontoLiquidacion"
	   ,FACT."ModPrefFact"
	   ,FACT."IdTipoComprobante"
	   ,FACT."IdTipoOperacion"	   
	   ,Ind."DetalleIndicador" 							AS "ModoFacturacion"
	   ,FACT."IdEmpresa"
	   ,C."IdContratoComision"
	   ,C."IdTipoContrato"
	   ,C."Nombre"										AS "NombreContrato"
	   ,C."IdPortafolio"								AS "IdPortafolioContrato"
	   ,C."IdFormatoCobro"
	   ,FACT."IdConcepto"								AS "IdConceptoFactura"
 	-- CONCEPTO   
	   ,CASE 
	   		WHEN COFI."IdCobro" = 'COFI' THEN COFI."IdConcepto"
	   		WHEN COVA."IdCobro" = 'COVA' THEN COVA."IdConcepto"
	   		WHEN DEFI."IdCobro" = 'DEFI' THEN DEFI."IdConcepto"
	   		WHEN DEVA."IdCobro" = 'DEVA' THEN DEVA."IdConcepto"	   		
	   	END 									 		AS "IdConceptoContrato"
 	-- COBRO COMISION	   
	   ,CASE 
	   		WHEN COFI."IdCobro" = 'COFI' THEN COFI."IdCobro"
	   		WHEN COVA."IdCobro" = 'COVA' THEN COVA."IdCobro"
	   	END 									 		AS "IdCobroComision"
	   ,CASE 
	   		WHEN COFI."IdCobro" = 'COFI' THEN COFI."EsPorRangos"
	   		WHEN COVA."IdCobro" = 'COVA' THEN COVA."EsPorRangos"
	   	END 									 		AS "EsPorRangosComision"	   	
	   ,CASE 
	   		WHEN COFI."IdCobro" = 'COFI' THEN 
	   			(CASE WHEN COFI."EsPorRangos" = 'N' THEN COALESCE(COFI."Monto",0) ELSE COALESCE(COFI_POR_RANGO."Monto",0) END)
			WHEN COVA."IdCobro" = 'COVA'	THEN 
	   			(CASE WHEN COVA."EsPorRangos" = 'N' THEN ROUND((FACT."MontoLiquidacion" * COALESCE(COVA."Porcentaje",0))/100) ELSE ROUND((FACT."MontoLiquidacion" * COALESCE(COVA_POR_RANGO."Porcentaje",0))/100) END)
	   	END 									 		AS "MontoComision"
 	-- COBRO DERECHO
	   ,CASE 
	   		WHEN DEFI."IdCobro" = 'DEFI' THEN DEFI."IdCobro"
	   		WHEN DEVA."IdCobro" = 'DEVA' THEN DEVA."IdCobro"
	   	END 									 		AS "IdCobroDerecho"
	   ,CASE 
	   		WHEN DEFI."IdCobro" = 'DEFI' THEN DEFI."EsPorRangos"
	   		WHEN DEVA."IdCobro" = 'DEVA' THEN DEVA."EsPorRangos"
	   	END 									 		AS "EsPorRangosDerecho"	   	
	   ,CASE 
	   		WHEN DEFI."IdCobro" = 'DEFI' THEN 
	   			(CASE WHEN DEFI."EsPorRangos" = 'N' THEN COALESCE(DEFI."Monto",0) ELSE COALESCE(DEFI_POR_RANGO."Monto",0) END)
			WHEN DEVA."IdCobro" = 'DEVA'	THEN 
	   			(CASE WHEN DEVA."EsPorRangos" = 'N' THEN ROUND((FACT."MontoLiquidacion" * COALESCE(DEVA."Porcentaje",0))/100) ELSE ROUND((FACT."MontoLiquidacion" * COALESCE(DEVA_POR_RANGO."Porcentaje",0))/100) END)
	   	END 									 		AS "MontoDerecho"	   	
 	-- CALCULO COBRO COMISION FIJA
	   ,COFI."IdCobro"							 		AS "IdCobroCOFI"
	   ,COFI."EsPorRangos"						 		AS "EsPorRangosCOFI"
	   ,COALESCE(COFI."Monto",0) 				 		AS "MontoCOFI"
	   ,COALESCE(COFI_POR_RANGO."Monto",0) 		 		AS "MontoCOFIPorRango"
	   ,COALESCE(COFI_POR_RANGO."MontoMaximo",0) 		AS "MontoMaximoCOFIPorRango"
 	-- CALCULO COBRO COMISION POR RANGO	   
	   ,COVA."IdCobro"							 		AS "IdCobroCOVA"
	   ,COVA."EsPorRangos"						 		AS "EsPorRangosCOVA"	   
	   ,COALESCE(COVA."Porcentaje",0) 			 		AS "PorcentajeCOVA"
	   ,COALESCE(COVA_POR_RANGO."Porcentaje",0)  		AS "PorcentajeCOVAPorRango"
	   ,COALESCE(COVA_POR_RANGO."MontoMaximo",0) 		AS "MontoMaximoCOVAPorRango"
 	-- CALCULO COBRO DERECHO FIJO	   
	   ,DEFI."IdCobro"							 		AS "IdCobroDEFI"
	   ,DEFI."EsPorRangos"						 		AS "EsPorRangosDEFI"
	   ,COALESCE(DEFI."Monto",0) 				 		AS "MontoDEFI"
	   ,COALESCE(DEFI_POR_RANGO."Monto",0) 		 		AS "MontoDEFIPorRango"
	   ,COALESCE(DEFI_POR_RANGO."MontoMaximo",0) 		AS "MontoMaximoDEFIPorRango"
 	-- CALCULO COBRO DERECHO POR RANGO	   
	   ,DEVA."IdCobro"							 		AS "IdCobroDEVA"
	   ,DEVA."EsPorRangos"						 		AS "EsPorRangosDEVA"	   
	   ,COALESCE(DEVA."Porcentaje",0) 			 		AS "PorcentajeDEVA"
	   ,COALESCE(DEVA_POR_RANGO."Porcentaje",0)  		AS "PorcentajeDEVAPorRango"
	   ,COALESCE(DEVA_POR_RANGO."MontoMaximo",0) 		AS "MontoMaximoDEVAPorRango"	   
	   
   FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS FACT
  INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON ( POR."IdPortafolio" = FACT."IdPortafolio" )  
   LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" AS Ind ON Ind."Codigo" = FACT."ModPrefFact"  
  INNER JOIN "BSCL.Plataforma.BD::COM.ContratoComision" AS C ON 
      ( C."IdMoneda" 						= FACT."IdMoneda"      
    AND C."IdCliente" 						= FACT."IdCliente" 
    AND C."IdClasificacionTipoInstrumento" 	= FACT."IdClasificacionTipoInstrumento" 
    AND C."IdTipoContrato"					= 'CONT' )
   INNER JOIN "BSCL.Plataforma.BD::COM.DetalleContratoComision" AS CD ON 
      ( CD."IdContratoComision" = C."IdContratoComision" AND CD."IdConcepto" = FACT."IdConcepto" )    
    -- COMISION FIJA POR MONTO
 LEFT JOIN (  SELECT "IdContratoComision" ,"IdCobro", "EsPorRangos" ,"Monto", "IdConcepto"
			    FROM "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoLineal"
		       WHERE "IdCobro" = 'COFI' ) AS COFI ON COFI."IdContratoComision" = C."IdContratoComision"    
    -- COMISION FIJA POR MONTO DE RANGO
 LEFT JOIN (  SELECT "IdComprobante", "IdContratoComision", "Monto", "MontoMaximo", "IdConcepto"
			    FROM "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoPorRango"
		       WHERE "IdCobro" = 'COFI' ) AS COFI_POR_RANGO ON COFI_POR_RANGO."IdComprobante" = FACT."IdComprobante" AND COFI_POR_RANGO."IdContratoComision" = C."IdContratoComision"
	-- COMISION VARIABLE POR PORCENTAJE					
 LEFT JOIN (  SELECT "IdContratoComision", "IdCobro", "EsPorRangos", "Porcentaje", "IdConcepto"
			    FROM "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoLineal"
		       WHERE "IdCobro" = 'COVA' ) AS COVA ON COVA."IdContratoComision" = C."IdContratoComision"    
	-- COMISION VARIABLE POR PORCENTAJE DE RANGO
 LEFT JOIN (  SELECT "IdComprobante", "IdContratoComision", "IdCobro", "Porcentaje", "MontoMaximo", "IdConcepto"
			    FROM "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoPorRango"
		       WHERE "IdCobro" = 'COVA' ) AS COVA_POR_RANGO ON COVA_POR_RANGO."IdComprobante" = FACT."IdComprobante" AND COVA_POR_RANGO."IdContratoComision" = C."IdContratoComision"
   -- DERECHO FIJO POR MONTO
 LEFT JOIN (  SELECT "IdContratoComision" ,"IdCobro", "EsPorRangos" ,"Monto", "IdConcepto"
			    FROM "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoLineal"
		       WHERE "IdCobro" = 'DEFI' ) AS DEFI ON DEFI."IdContratoComision" = C."IdContratoComision"    
    -- DERECHO FIJO POR MONTO DE RANGO		       
 LEFT JOIN (  SELECT "IdComprobante", "IdContratoComision", "Monto", "MontoMaximo", "IdConcepto"
			    FROM "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoPorRango"
		       WHERE "IdCobro" = 'DEFI' ) AS DEFI_POR_RANGO ON DEFI_POR_RANGO."IdComprobante" = FACT."IdComprobante" AND DEFI_POR_RANGO."IdContratoComision" = C."IdContratoComision"
	-- DERECHO VARIABLE POR PORCENTAJE					
 LEFT JOIN (  SELECT "IdContratoComision", "IdCobro", "EsPorRangos", "Porcentaje", "IdConcepto"
			    FROM "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoLineal"
		       WHERE "IdCobro" = 'DEVA' ) AS DEVA ON DEVA."IdContratoComision" = C."IdContratoComision"    
	-- DERECHO VARIABLE POR PORCENTAJE DE RANGO		       
 LEFT JOIN (  SELECT "IdComprobante", "IdContratoComision", "IdCobro", "Porcentaje", "MontoMaximo", "IdConcepto"
			    FROM "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoPorRango"
		       WHERE "IdCobro" = 'DEVA' ) AS DEVA_POR_RANGO ON DEVA_POR_RANGO."IdComprobante" = FACT."IdComprobante" AND DEVA_POR_RANGO."IdContratoComision" = C."IdContratoComision"		       
WHERE FACT."ModPrefFact" = 'MODPREFCONT';

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContrato" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
