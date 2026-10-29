	WITH NORConfiguracion AS (
		-- Parametro para identificar si se calculan los campos Valor Par, Precio Compra, Duracion Modificada y Convexidad
		SELECT  "IdEmpresa"
		       ,"Valor"				AS "SIGIRP40"
		  FROM "BSCL.Normativa.BD::NOR.Configuracion"
		WHERE "IdEmpresa"	= :IdEmpresa
		  AND "CodigoParametro"	= 'P40SIGIRP40' 
	)
	SELECT    
  	         TO_VARCHAR(OPER."FechaOperacion", 'YYYYMMDD') 										AS "FechaInversion"
	        ,DET."IdClasificacionTipoInstrumento"
	        ,CAR."IdTipoInstrumento"
	        ,RPAD(COALESCE(EMI."DocumentoIdentidad",''), 10, '0')								AS "RutEmisor"
			,CASE WHEN DET."IdClasificacionTipoInstrumento" = 'RF' THEN
				(CASE WHEN CAR."IdTipoInstrumento" = 'BR'  THEN '03'
					  WHEN CAR."IdTipoInstrumento" = 'LTH' THEN '04'
					  WHEN CAR."IdTipoInstrumento" = 'BEM' THEN '06'					
					  WHEN CAR."IdTipoInstrumento" = 'BBC' THEN '08'					
				      ELSE '01' 
				 END)
			     ELSE '10'
			 END																				AS "Familia"	
			      
	        ,CASE WHEN DET."IdClasificacionTipoInstrumento" = 'IF' AND UCASE(:UsaNemoSVS) = 'S' 
	        	  THEN COALESCE(CAR."NemoSVS", 'INS.Nemotecnico')
	              ELSE COALESCE(INS."Nemotecnico", 'INS.Nemotecnico')
	         END 																				AS "Nemo"
	              
	        ,CASE WHEN CAR."INGNumeroCupon" = 1 AND CAR."IdTipoInstrumento" IN ('CER', 'XER', 'ZER') THEN '1'	-- 1 Cero cupon
	        	  WHEN CAR."INGNumeroCupon" > 1 AND CAR."IdTipoInstrumento" IN ('BCP', 'BCU') THEN '2'		  	-- 2 Bullet
	        	  WHEN CAR."IdTipoInstrumento" = 'LTH' THEN '3'												  	-- 3 Cupon constante (Amortizacion Francesa)	
	         	  ELSE '9' 																					  	-- 9 Otro
	         END 																				AS "TipoRendimiento"							  	
	         	  
	        ,CASE WHEN INS."FrecuenciaCupon" = 0  THEN '0' 														-- 0 Cero cupon
	        	  WHEN INS."FrecuenciaCupon" = 1  THEN '1'		  												-- 1 Mensual
	        	  WHEN INS."FrecuenciaCupon" = 3  THEN '2'												  		-- 2 Trimestral	
	        	  WHEN INS."FrecuenciaCupon" = 4  THEN '3'		  												-- 3 Cuatrimestral
	        	  WHEN INS."FrecuenciaCupon" = 6  THEN '4'		  												-- 4 Semestral                	  
	        	  WHEN INS."FrecuenciaCupon" = 12 THEN '5'												  		-- 5 Anual                	  
	         	  ELSE '9' 																					  	-- 9 Otro
	         END 																				AS "PeriodoCupon"	
	
	        ,COALESCE (TO_VARCHAR(CAR."INGFechaCorteCupon" , 'YYYYMMDD'),'00010101')			AS "FechaUltimoCupon"
	        ,COALESCE (TO_VARCHAR(CAR."INGFechaPagoCorteCupon" , 'YYYYMMDD'),'00010101')		AS "FechaProximoCupon"
	        ,COALESCE (TO_VARCHAR(CAR."FechaVencimientoInstrumento" , 'YYYYMMDD'),'00010101')   AS "FechaVencimientoInstrumento"
	        ,CAR."IdTipoReajuste"
	        ,COALESCE(CAR."Cantidad", 0)														AS "NominalInicial"
	        ,COALESCE(CAR."INGCantidad", 0)        												AS "NominalActual"
	        ,CASE WHEN CAR."IdTipoReajuste" = 'EUR' THEN '142'
	        	  WHEN CAR."IdTipoReajuste" IN ('USD', 'DO') THEN '994'	-- Dolar Observado
	        	  WHEN CAR."IdTipoReajuste" = 'IV'  THEN '997'
	        	  WHEN CAR."IdTipoReajuste" = 'UF'  THEN '998'
	        	  WHEN COALESCE(CAR."IdTipoReajuste",'') IN ('','NR')  THEN '999'
	         	  ELSE '880'
	         END 																				AS "MonedaNominal"
	         
			,CASE WHEN DET."IdClasificacionTipoInstrumento" = 'IF' THEN
				(CASE WHEN COALESCE(CAR."IdTipoReajuste",'NR') = 'NR'  THEN '1193000'
				      ELSE '1191000' 
				 END)
			     ELSE '0000000'
			 END																				AS "TipoTasaIF20"
			 
			,CASE WHEN DET."IdClasificacionTipoInstrumento" = 'IF' THEN
				(CASE WHEN COALESCE(CAR."IdTipoReajuste",'NR') = 'NR'  THEN '1193000'
				      ELSE '1191000' 
				 END)
			     ELSE '0000000'
			 END																				AS "TipoTasaIF24_29"

			,COALESCE(PV."AnoBase", 0)															AS "AnoBase"
			,COALESCE(PV."ComputoDeDias", 0)													AS "ComputoDeDias"			
			,COALESCE(PV."TipoCalculo", 1)														AS "TipoCalculo"	-- Fija (1) o Flotante (2)			
			,COALESCE(PV."TipoTasaPago", 0)														AS "TipoTasaPago"	-- Compuesta (0) o lineal (1)														
			,COALESCE(PV."TasaPago", 0)															AS "TasaPago"			
			
			,CASE WHEN CAR."IdTipoInstrumento" = 'BR'  THEN '0400'
				  ELSE '0000' 
			 END																				AS "TasaEmision"
			,COALESCE(INS."Tera", 0)															AS "Tera"	
			
   		 	 -- identifica si calcula el campo Valor-Par			
			,CASE WHEN CONF."SIGIRP40" = 'N' THEN 0
			      ELSE COALESCE(CAR."INGMontoCompraActualMonedaInstrumento", 0)											
			 END    											 								AS "ValorPar"
			 
   		 	,CASE WHEN CAR."TasaCompra" >= 0 THEN 
   		 			LPAD(REPLACE(CAST(CAR."TasaCompra" AS VARCHAR(4)),'.',''),4,'0') || '+'
   		 		 WHEN CAR."TasaCompra" < 0 THEN
   		 		 	REPLACE(LPAD(REPLACE(CAST(CAR."TasaCompra" AS VARCHAR(6)),'.',''),5,'0'),'-','') || '-'
   		 		 ELSE '0000+' 
   		 	 END 																				AS "TasaCompra25"
			,COALESCE(CAR."CostoInversionEnFactorAjuste", 0)									AS "CostoAdquisicion"
			,COALESCE(CAR."INGTasaMercadoActual", 0)											AS "TasaCompra27"
	        ,COALESCE(CAR."INGMontoATasaMercadoActual", 0)										AS "ValorRazonable"
   		 	,CASE WHEN CAR."INGMontoValorRazonableActivo" >= 0 THEN 
   		 			LPAD(REPLACE(CAST(CAR."INGMontoValorRazonableActivo" AS VARCHAR(4)),'.',''),4,'0') || '+'
   		 		 WHEN CAR."INGMontoValorRazonableActivo" < 0 THEN
   		 		 	REPLACE(LPAD(REPLACE(CAST(CAR."INGMontoValorRazonableActivo" AS VARCHAR(6)),'.',''),5,'0'),'-','') || '-'
   		 		 ELSE '0000+' 
   		 	 END 																				AS "TasaValorizacion"	
   		 	,CONF."SIGIRP40"
   		 	 -- identifica si calcula los campos Precio Compra, Duración Modificada y Convexidad
			,CASE WHEN CONF."SIGIRP40" = 'N' THEN 0
			      ELSE COALESCE(CAR."PrecioCompra", 0)											
			 END    											 								AS "PrecioCompra"   		 	 
			,CASE WHEN CONF."SIGIRP40" = 'N' THEN 0
			      ELSE COALESCE(CAR."INGDurationCompraActual", 0)											
			 END    											 								AS "DuracionModificada"  
			,CASE WHEN CONF."SIGIRP40" = 'N' THEN 0
			      ELSE COALESCE(CAR."INGConvexidadCompraActual", 0)											
			 END    											 								AS "Convexidad"  			 
			,'1'																				AS "CondicionInstrumento" 
	        ,'00000000'																			AS "FechaInicioCond"
	        ,'00000000'																			AS "FechaTermCond"	     
	        ,TO_VARCHAR(COALESCE(CPO."FechaLiquidacionComprobantePagoOperacion", '0001-01-01'), 'YYYY-MM-DD')	AS "FechaVencCompAdj"
	        ,RPAD(COALESCE(CAR."IdEmisor",''), 10, '0')											AS "Emisor"
	FROM    "BSCL.Mesa.BD::MES.Operacion" 	AS OPER
    LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS DET ON DET."IdOperacion" = OPER."IdOperacion"
    LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."IdOperacion" = DET."IdOperacion"
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON DET."IdInstrumento" = INS."IdInstrumento"
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS EMI ON EMI."IdEmisor" = INS."IdEmisor"    
    LEFT JOIN "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" CPO ON CPO."IdOperacion" = OPER."IdOperacion"
    LEFT JOIN "BSCL.Plataforma.BD::INS.ParametroValorizador" PV ON PV."IdOrdenSerie" = INS."IdOrdenSerie"    
    LEFT JOIN  NORConfiguracion CONF ON CONF."IdEmpresa" = OPER."IdEmpresa"
	WHERE OPER."IdEmpresa"						 = :IdEmpresa
	  AND OPER."IdEstado" 						 = 'VIG'
	  AND ((DET."IdClasificacionTipoInstrumento" IN ('RF','IF') AND :Mercado = '') OR (DET."IdClasificacionTipoInstrumento" = :Mercado))
	  AND OPER."IdCliente" 						 = :IdCliente
UNION
	SELECT    
	         TO_VARCHAR(G."FechaOperacion", 'YYYYMMDD') 										AS "FechaInversion"
	        ,G."IdClasificacionTipoInstrumento"
	        ,G."IdTipoInstrumento"
	        ,RPAD(COALESCE(EMI."DocumentoIdentidad",''), 10, '0')								AS "RutEmisor"	        
			,CASE WHEN G."IdClasificacionTipoInstrumento" = 'RF' THEN
				(CASE WHEN G."IdTipoInstrumento" = 'BR'  THEN '03'
					  WHEN G."IdTipoInstrumento" = 'LTH' THEN '04'
					  WHEN G."IdTipoInstrumento" = 'BEM' THEN '06'					
					  WHEN G."IdTipoInstrumento" = 'BBC' THEN '08'					
				      ELSE '01' 
				 END)
			     ELSE '10'
			 END																				AS "Familia"	
			      
	        ,CASE WHEN G."IdClasificacionTipoInstrumento" = 'IF' AND UCASE(:UsaNemoSVS) = 'S' 
	        	  THEN COALESCE(G."NemoSVS", 'INS.Nemotecnico')
	              ELSE COALESCE(G."Nemotecnico", 'INS.Nemotecnico')
	         END 																				AS "Nemo"
	              
	        ,CASE WHEN G."INGNumeroCupon" = 1 AND G."IdTipoInstrumento" IN ('CER', 'XER', 'ZER') THEN '1'		-- 1 Cero cupon
	        	  WHEN G."INGNumeroCupon" > 1 AND G."IdTipoInstrumento" IN ('BCP', 'BCU') THEN '2'		  		-- 2 Bullet
	        	  WHEN G."IdTipoInstrumento" = 'LTH' THEN '3'												  	-- 3 Cupon constante (Amortizacion Francesa)	
	         	  ELSE '9' 																					  	-- 9 Otro
	         END 																				AS "TipoRendimiento"							  	
	         	  
	        ,CASE WHEN INS."FrecuenciaCupon" = 0  THEN '0' 														-- 0 Cero cupon
	        	  WHEN INS."FrecuenciaCupon" = 1  THEN '1'		  												-- 1 Mensual
	        	  WHEN INS."FrecuenciaCupon" = 3  THEN '2'												  		-- 2 Trimestral	
	        	  WHEN INS."FrecuenciaCupon" = 4  THEN '3'		  												-- 3 Cuatrimestral
	        	  WHEN INS."FrecuenciaCupon" = 6  THEN '4'		  												-- 4 Semestral                	  
	        	  WHEN INS."FrecuenciaCupon" = 12 THEN '5'												  		-- 5 Anual                	  
	         	  ELSE '9' 																					  	-- 9 Otro
	         END 																				AS "PeriodoCupon"	
	
	        ,COALESCE (TO_VARCHAR(G."INGFechaCorteCupon" , 'YYYYMMDD'),'00010101')				AS "FechaUltimoCupon"
	        ,COALESCE (TO_VARCHAR(G."INGFechaPagoCorteCupon" , 'YYYYMMDD'),'00010101')			AS "FechaProximoCupon"
	        ,COALESCE (TO_VARCHAR(G."FechaVencimientoInstrumento" , 'YYYYMMDD'),'00010101')   	AS "FechaVencimientoInstrumento"
	        ,G."IdTipoReajuste"
	        ,COALESCE(G."Cantidad", 0)															AS "NominalInicial"
	        ,COALESCE(G."INGCantidad", 0)        												AS "NominalActual"
	        ,CASE WHEN G."IdTipoReajuste" = 'EUR' THEN '142'
	        	  WHEN G."IdTipoReajuste" IN ('USD', 'DO') THEN '994'
	        	  WHEN G."IdTipoReajuste" = 'IV'  THEN '997'		
	        	  WHEN G."IdTipoReajuste" = 'UF'  THEN '998'		
	        	  WHEN COALESCE(G."IdTipoReajuste",'') IN ('','NR')  THEN '999'                	  
	         	  ELSE '880'
	         END 																				AS "MonedaNominal"
	         
			,CASE WHEN G."IdClasificacionTipoInstrumento" = 'IF' THEN
				(CASE WHEN COALESCE(G."IdTipoReajuste",'NR') = 'NR'  THEN '1193000'
				      ELSE '1191000' 
				 END)
			     ELSE '0000000'
			 END																				AS "TipoTasaIF20"
			 
			,CASE WHEN G."IdClasificacionTipoInstrumento" = 'IF' THEN
				(CASE WHEN COALESCE(G."IdTipoReajuste",'NR') = 'NR'  THEN '1193000'
				      ELSE '1191000' 
				 END)
			     ELSE '0000000'
			 END																				AS "TipoTasaIF24_29"

			,COALESCE(PV."AnoBase", 0)															AS "AnoBase"
			,COALESCE(PV."ComputoDeDias", 0)													AS "ComputoDeDias"			
			,COALESCE(PV."TipoCalculo", 1)														AS "TipoCalculo"	-- Fija (1) o Flotante (2)			
			,COALESCE(PV."TipoTasaPago", 0)														AS "TipoTasaPago"	-- Compuesta (0) o lineal (1)														
			,COALESCE(PV."TasaPago", 0)															AS "TasaPago"	
			
			,CASE WHEN G."IdTipoInstrumento" = 'BR'  THEN '0400'
				  ELSE '0000' 
			 END																				AS "TasaEmision"
			,COALESCE(INS."Tera", 0)															AS "Tera"	
			
   		 	 -- identifica si calcula el campo Valor-Par			
			,CASE WHEN CONF."SIGIRP40" = 'N' THEN 0
			      ELSE COALESCE(G."INGMontoCompraActualMonedaInstrumento", 0)											
			 END    											 								AS "ValorPar"
			 
   		 	,CASE WHEN G."TasaCompraOperacionOrigen" >= 0 THEN 
   		 			LPAD(REPLACE(CAST(G."TasaCompraOperacionOrigen" AS VARCHAR(4)),'.',''),4,'0') || '+'
   		 		 WHEN G."TasaCompraOperacionOrigen" < 0 THEN
					REPLACE(LPAD(REPLACE(CAST(G."TasaCompraOperacionOrigen" AS VARCHAR(6)),'.',''),5,'0'),'-','') || '-'
   		 		 ELSE '0000+' 
   		 	 END 																				AS "TasaCompra25"
			,COALESCE(G."MontoGarantia", 0)														AS "CostoAdquisicion"
			,COALESCE(CAR."INGTasaMercadoActual", 0)											AS "TasaCompra27"
	        ,COALESCE(CAR."INGMontoATasaMercadoActual", 0)										AS "ValorRazonable"
   		 	,CASE WHEN CAR."INGMontoValorRazonableActivo" >= 0 THEN 
   		 			LPAD(REPLACE(CAST(CAR."INGMontoValorRazonableActivo" AS VARCHAR(4)),'.',''),4,'0') || '+'
   		 		 WHEN CAR."INGMontoValorRazonableActivo" < 0 THEN
   		 		 	REPLACE(LPAD(REPLACE(CAST(CAR."INGMontoValorRazonableActivo" AS VARCHAR(6)),'.',''),5,'0'),'-','') || '-'
   		 		 ELSE '0000+' 
   		 	 END 																				AS "TasaValorizacion"	
   		 	 -- identifica si calcula los campos Precio Compra, Duración Modificada y Convexidad
   		 	,CONF."SIGIRP40" 
			,CASE WHEN CONF."SIGIRP40" = 'N' THEN 0
			      ELSE COALESCE(G."PrecioCompraOperacionOrigen", 0)											
			 END    											 								AS "PrecioCompra"   		 	 
			,CASE WHEN CONF."SIGIRP40" = 'N' THEN 0
			      ELSE COALESCE(G."INGDurationCompraActual", 0)											
			 END    											 								AS "DuracionModificada"  
			,CASE WHEN CONF."SIGIRP40" = 'N' THEN 0
			      ELSE COALESCE(G."INGConvexidadCompraActual", 0)											
			 END    											 								AS "Convexidad"  			 
			,'3'																				AS "CondicionInstrumento" 
			
	        ,CASE WHEN COALESCE(TO_VARCHAR(G."FechaOperacion", 'YYYYMMDD'),'00010101') = '00010101' OR
	        	  	   COALESCE(TO_VARCHAR(G."FechaOperacion", 'YYYYMMDD'),'19000101') = '19000101' THEN '99999999'
	              ELSE
	              	TO_VARCHAR(G."FechaOperacion", 'YYYYMMDD')
	         END																				 AS "FechaInicioCond"
	         
	        ,CASE WHEN COALESCE(TO_VARCHAR(G."FechaVencimientoOperacion", 'YYYYMMDD'),'00010101') = '00010101' OR
	        	  	   COALESCE(TO_VARCHAR(G."FechaVencimientoOperacion", 'YYYYMMDD'),'19000101') = '19000101' THEN '99999999'
	              ELSE
	              	TO_VARCHAR(G."FechaVencimientoOperacion", 'YYYYMMDD')
	         END																				AS "FechaTermCond"	
	        
	        ,TO_VARCHAR(COALESCE(CPO."FechaLiquidacionComprobantePagoOperacion", '0001-01-01'), 'YYYY-MM-DD')	AS "FechaVencCompAdj"
	        ,RPAD(COALESCE(CAR."IdEmisor",''), 10, '0')											AS "Emisor"
	FROM    "BSCL.Custodia.BD::GTA.Garantia" G
    LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON (CAR."IdOperacion" = G."IdCarteraOrigen")
    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON (CLI."IdEmpresa" = CAR."IdEmpresa" AND CLI."IdCliente" = CAR."IdCliente")    
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON (INS."IdInstrumento" = G."IdInstrumento")
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS EMI ON EMI."IdEmisor" = INS."IdEmisor"    
	LEFT JOIN "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" CPO ON (CPO."IdOperacion" = CAR."IdOperacion")
    LEFT JOIN "BSCL.Plataforma.BD::INS.ParametroValorizador" PV ON (PV."IdOrdenSerie" = INS."IdOrdenSerie")	
    LEFT JOIN  NORConfiguracion CONF ON CONF."IdEmpresa" = G."IdEmpresa"
	WHERE G."IdEmpresa"							= :IdEmpresa
	  AND G."IdEstado" 							= 'VIG'
	  AND ((G."IdClasificacionTipoInstrumento" 	IN ('RF','IF') AND :Mercado = '') OR (G."IdClasificacionTipoInstrumento" = :Mercado))
	  AND G."IdCliente" 						= :IdCliente
UNION	
	SELECT    
	         TO_VARCHAR(SUBY."INGFechaOperacion", 'YYYYMMDD') 									AS "FechaInversion"
	        ,SUBY."IdClasificacionTipoInstrumento"
	        ,SUBY."IdTipoInstrumento"
	        ,RPAD(COALESCE(EMI."DocumentoIdentidad",''), 10, '0')								AS "RutEmisor"	        
			,CASE WHEN SUBY."IdClasificacionTipoInstrumento" = 'RF' THEN
				(CASE WHEN SUBY."IdTipoInstrumento" = 'BR'  THEN '03'
					  WHEN SUBY."IdTipoInstrumento" = 'LTH' THEN '04'
					  WHEN SUBY."IdTipoInstrumento" = 'BEM' THEN '06'					
					  WHEN SUBY."IdTipoInstrumento" = 'BBC' THEN '08'					
				      ELSE '01' 
				 END)
			     ELSE '10'
			 END																				AS "Familia"	
			      
	        ,CASE WHEN SUBY."IdClasificacionTipoInstrumento" = 'IF' AND UCASE(:UsaNemoSVS) = 'S' 
	        	  THEN COALESCE(SUBY."NemoSVS", 'INS.Nemotecnico')
	              ELSE COALESCE(SUBY."Nemotecnico", 'INS.Nemotecnico')
	         END 																				AS "Nemo"
	              
	        ,CASE WHEN SUBY."INGNumeroCupon" = 1 AND SUBY."IdTipoInstrumento" IN ('CER', 'XER', 'ZER') THEN '1'	-- 1 Cero cupon
	        	  WHEN SUBY."INGNumeroCupon" > 1 AND SUBY."IdTipoInstrumento" IN ('BCP', 'BCU') THEN '2'		-- 2 Bullet
	        	  WHEN SUBY."IdTipoInstrumento" = 'LTH' THEN '3'												-- 3 Cupon constante (Amortizacion Francesa)	
	         	  ELSE '9' 																					  	-- 9 Otro
	         END 																				AS "TipoRendimiento"							  	
	         	  
	        ,CASE WHEN INS."FrecuenciaCupon" = 0  THEN '0' 														-- 0 Cero cupon
	        	  WHEN INS."FrecuenciaCupon" = 1  THEN '1'		  												-- 1 Mensual
	        	  WHEN INS."FrecuenciaCupon" = 3  THEN '2'												  		-- 2 Trimestral	
	        	  WHEN INS."FrecuenciaCupon" = 4  THEN '3'		  												-- 3 Cuatrimestral
	        	  WHEN INS."FrecuenciaCupon" = 6  THEN '4'		  												-- 4 Semestral                	  
	        	  WHEN INS."FrecuenciaCupon" = 12 THEN '5'												  		-- 5 Anual                	  
	         	  ELSE '9' 																					  	-- 9 Otro
	         END 																				AS "PeriodoCupon"	
	
	        ,COALESCE (TO_VARCHAR(SUBY."INGFechaCorteCupon" , 'YYYYMMDD'),'00010101')			AS "FechaUltimoCupon"
	        ,COALESCE (TO_VARCHAR(SUBY."INGFechaPagoCorteCupon" , 'YYYYMMDD'),'00010101')		AS "FechaProximoCupon"
	        ,COALESCE (TO_VARCHAR(SUBY."FechaVencimientoInstrumento" , 'YYYYMMDD'),'00010101')  AS "FechaVencimientoInstrumento"
	        ,SUBY."IdTipoReajuste"
	        ,COALESCE(SUBY."Cantidad", 0)														AS "NominalInicial"
	        ,COALESCE(SUBY."INGCantidad", 0)        											AS "NominalActual"
	        ,CASE WHEN SUBY."IdTipoReajuste" = 'EUR' THEN '142'
	        	  WHEN SUBY."IdTipoReajuste" IN ('USD', 'DO') THEN '994'
	        	  WHEN SUBY."IdTipoReajuste" = 'IV'  THEN '997'		
	        	  WHEN SUBY."IdTipoReajuste" = 'UF'  THEN '998'		
	        	  WHEN COALESCE(SUBY."IdTipoReajuste",'') IN ('','NR')  THEN '999'                	  
	         	  ELSE '880'
	         END 																				AS "MonedaNominal"
	         
			,CASE WHEN SUBY."IdClasificacionTipoInstrumento" = 'IF' THEN
				(CASE WHEN COALESCE(SUBY."IdTipoReajuste",'NR') = 'NR'  THEN '1193000'
				      ELSE '1191000' 
				 END)
			     ELSE '0000000'
			 END																				AS "TipoTasaIF20"
			 
			,CASE WHEN SUBY."IdClasificacionTipoInstrumento" = 'IF' THEN
				(CASE WHEN COALESCE(SUBY."IdTipoReajuste",'NR') = 'NR'  THEN '1193000'
				      ELSE '1191000' 
				 END)
			     ELSE '0000000'
			 END																				AS "TipoTasaIF24_29"

			,COALESCE(PV."AnoBase", 0)															AS "AnoBase"
			,COALESCE(PV."ComputoDeDias", 0)													AS "ComputoDeDias"			
			,COALESCE(PV."TipoCalculo", 1)														AS "TipoCalculo"	-- Fija (1) o Flotante (2)			
			,COALESCE(PV."TipoTasaPago", 0)														AS "TipoTasaPago"	-- Compuesta (0) o lineal (1)														
			,COALESCE(PV."TasaPago", 0)															AS "TasaPago"			
			
			,CASE WHEN SUBY."IdTipoInstrumento" = 'BR'  THEN '0400'
				  ELSE '0000' 
			 END																				AS "TasaEmision"
			,COALESCE(INS."Tera", 0)															AS "Tera"	
			
   		 	 -- identifica si calcula el campo Valor-Par			
			,CASE WHEN CONF."SIGIRP40" = 'N' THEN 0
			      ELSE COALESCE(SUBY."INGMontoCompraActualMonedaInstrumento", 0)											
			 END    											 								AS "ValorPar"
			 
   		 	,CASE WHEN SUBY."TasaCompraOperacionRelacionada" >= 0 THEN 
   		 			LPAD(REPLACE(CAST(SUBY."TasaCompraOperacionRelacionada" AS VARCHAR(4)),'.',''),4,'0') || '+'
   		 		 WHEN SUBY."TasaCompraOperacionRelacionada" < 0 THEN
   		 		 	REPLACE(LPAD(REPLACE(CAST(SUBY."TasaCompraOperacionRelacionada" AS VARCHAR(6)),'.',''),5,'0'),'-','') || '-'
   		 		 ELSE '0000+' 
   		 	 END 																				AS "TasaCompra25"
			,COALESCE(SUBY."MontoSubyacente", 0)												AS "CostoAdquisicion"
			,COALESCE(CAR."INGTasaMercadoActual", 0)											AS "TasaCompra27"			
	        ,COALESCE(CAR."INGMontoATasaMercadoActual", 0)										AS "ValorRazonable"
   		 	,CASE WHEN CAR."INGMontoValorRazonableActivo" >= 0 THEN 
   		 			LPAD(REPLACE(CAST(CAR."INGMontoValorRazonableActivo" AS VARCHAR(4)),'.',''),4,'0') || '+'
   		 		 WHEN CAR."INGMontoValorRazonableActivo" < 0 THEN
   		 		 	REPLACE(LPAD(REPLACE(CAST(CAR."INGMontoValorRazonableActivo" AS VARCHAR(6)),'.',''),5,'0'),'-','') || '-'
   		 		 ELSE '0000+' 
   		 	 END 																				AS "TasaValorizacion"	
   		 	 -- identifica si calcula los campos Precio Compra, Duración Modificada y Convexidad
   		 	,CONF."SIGIRP40" 
			,CASE WHEN CONF."SIGIRP40" = 'N' THEN 0
			      ELSE COALESCE(SUBY."PrecioCompraOperacionRelacionada", 0)											
			 END    											 								AS "PrecioCompra"   		 	 
			,CASE WHEN CONF."SIGIRP40" = 'N' THEN 0
			      ELSE COALESCE(SUBY."INGDurationCompraActual", 0)											
			 END    											 								AS "DuracionModificada"  
			,CASE WHEN CONF."SIGIRP40" = 'N' THEN 0
			      ELSE COALESCE(SUBY."INGConvexidadCompraActual", 0)											
			 END    											 								AS "Convexidad"  			 
			,'2'																				AS "CondicionInstrumento" 
	        ,TO_VARCHAR(SUBY."INGFechaOperacion", 'YYYYMMDD')									AS "FechaInicioCond"
	        ,TO_VARCHAR(CAR."FechaVencimientoOperacion", 'YYYYMMDD')							AS "FechaTermCond"	        
	        ,TO_VARCHAR(COALESCE(CPO."FechaLiquidacionComprobantePagoOperacion", '0001-01-01'), 'YYYY-MM-DD')	AS "FechaVencCompAdj"	        
	        ,RPAD(COALESCE(CAR."IdEmisor",''), 10, '0')											AS "Emisor"
	FROM    "BSCL.Custodia.BD::CUS.Subyacente" SUBY
    LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON (CAR."IdCartera" = SUBY."IdCarteraRetirada")
    LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" AS OPER ON (OPER."IdOperacion" = CAR."IdOperacion")
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON (INS."IdInstrumento" = SUBY."IdInstrumento")
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS EMI ON EMI."IdEmisor" = INS."IdEmisor"    
    LEFT JOIN "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" CPO ON (CPO."IdOperacion" = OPER."IdOperacion")
    LEFT JOIN "BSCL.Plataforma.BD::INS.ParametroValorizador" PV ON (PV."IdOrdenSerie" = INS."IdOrdenSerie")
    LEFT JOIN  NORConfiguracion CONF ON CONF."IdEmpresa" = CAR."IdEmpresa"
	WHERE CAR."IdEmpresa"						= :IdEmpresa
 	AND CAR."IdTipoOperacion"   				= 'COMPAC'
	AND SUBY."IdEstado" 						= 'VIG'
	AND ((SUBY."IdClasificacionTipoInstrumento" IN ('RF','IF') AND :Mercado = '') OR (SUBY."IdClasificacionTipoInstrumento" = :Mercado))
	AND CAR."IdCliente" 						= :IdCliente