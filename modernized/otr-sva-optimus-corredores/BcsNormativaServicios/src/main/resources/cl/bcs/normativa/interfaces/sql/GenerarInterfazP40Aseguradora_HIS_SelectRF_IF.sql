SELECT    
  	         TO_VARCHAR(CAR."FechaOperacion", 'YYYYMMDD') 										AS "FechaInversion"
	        ,CAR."IdClasificacionTipoInstrumento"
	        ,CAR."IdTipoInstrumento"
			,LPAD(COALESCE(CONCAT(SUBSTRING(EMI."DocumentoIdentidad",1,LENGTH(EMI."DocumentoIdentidad")-2),SUBSTRING(EMI."DocumentoIdentidad",LENGTH(EMI."DocumentoIdentidad"),1)),''),10,'0') AS "RutEmisor"
			,CASE 		
				WHEN (CAR."IdClasificacionTipoInstrumento" = 'IF' AND INS."Nemotecnico" IN ('PDBC','PRBC','PDT') ) THEN '49'
				WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' AND INS."Nemotecnico" NOT IN ('PDBC','PRBC','PDT')  THEN '10' 	
				WHEN CAR."IdTipoInstrumento" IN ('BTU','BTP','BCU','BCX','BCP','BCD','ZER', 'CER','XER') THEN '01'
				WHEN CAR."IdTipoInstrumento" = 'BR'  THEN '03'
				WHEN CAR."IdTipoInstrumento" = 'LTH' THEN '04'
				WHEN CAR."IdTipoInstrumento" = 'BEM' THEN '06'				
				WHEN CAR."IdTipoInstrumento" = 'BBC' THEN '08'
				WHEN (CAR."IdTipoInstrumento" = 'PAG' OR CAR."IdTipoInstrumento" = 'PAC') THEN '10'	
			ELSE '49' 	 END																	AS "Familia"	
			      
	        ,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' AND UCASE(:UsaNemoSVS) = 'S' 
	        	  THEN COALESCE(CAR."NemoSVS",INS."Nemotecnico")
	              ELSE COALESCE(CAR."Nemotecnico", INS."Nemotecnico")
	         END 																				AS "Nemo"
	         ,CAR."Nemotecnico" 																	AS "Nemotecnico" 
	              
	        ,CASE 	WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN '1'
	        		WHEN  INS."IdTipoInstrumento" = 'LH' THEN '3'
	        		WHEN (INS."NumeroAmortizaciones" = 1 AND  INS."NumeroCupones" = 1) THEN '1'
	        		WHEN (INS."NumeroAmortizaciones" = 1 AND  INS."NumeroCupones" > 1) THEN '2'
	        		ELSE '3'
	        END  AS "TipoRendimiento"								  	
	         	  
	        ,CASE WHEN ((CAR."IdClasificacionTipoInstrumento" = 'RF') AND 
	        			 CAR."IdTipoInstrumento" IN ('CER', 'XER', 'ZER', 'BR')) OR
	        		    (CAR."IdClasificacionTipoInstrumento" = 'IF') OR 
	        		   INS."FrecuenciaCupon" = 0  THEN '0' 													-- 0 Cero cupon
	        	  WHEN INS."FrecuenciaCupon" = 1  THEN '1'		  											-- 1 Mensual
	        	  WHEN INS."FrecuenciaCupon" = 3  THEN '2'													-- 2 Trimestral	
	        	  WHEN INS."FrecuenciaCupon" = 4  THEN '3'		  											-- 3 Cuatrimestral
	        	  WHEN INS."FrecuenciaCupon" = 6  THEN '4'		  											-- 4 Semestral                	  
	        	  WHEN INS."FrecuenciaCupon" = 12 THEN '5'													-- 5 Anual                	  
	         	  ELSE '9' 																					-- 9 Otro
	         END 																				AS "PeriodoCupon"	
	
	        , COALESCE ( TO_VARCHAR(ADF."Valor", 'YYYYMMDD')  ,'00010101' ) AS "FechaUltimoCupon"
			, COALESCE ( TO_VARCHAR(ADD_MONTHS  (ADF."Valor",INS."FrecuenciaCupon" )  , 'YYYYMMDD')  ,'00000000' ) 	AS "FechaProximoCupon"
	        
	     	,CASE when to_INTEGER(TO_VARCHAR(CAR."INGFechaPagoCorteCupon", 'YYYYMMDD')) > to_INTEGER(TO_VARCHAR(CAR."FechaVencimientoInstrumento" , 'YYYYMMDD')) 
	     			then COALESCE (TO_VARCHAR(CAR."INGFechaPagoCorteCupon" , 'YYYYMMDD'),'00010101') else
	        		COALESCE (TO_VARCHAR(CAR."FechaVencimientoInstrumento" , 'YYYYMMDD'),'00010101') end  AS "FechaVencimientoInstrumento"
	        ,CAR."IdTipoReajuste"
	        ,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN COALESCE(CAR."INGValorRescate", 0)	ELSE COALESCE(CAR."INGCantidad", 0)	 END AS "NominalInicial"
			,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN COALESCE(CAR."INGValorRescate", 0)	ELSE COALESCE(CAR."INGCantidad", 0)	 END AS "NominalActual"

	        ,CASE WHEN CAR."IdTipoReajuste" = 'EUR' THEN '142'
	        	  WHEN (CAR."IdTipoReajuste" = 'USD' AND
             	  		CAR."IdClasificacionTipoInstrumento" = 'IF' AND 
             	  		SUBSTRING(CAR."NemoSVS",1,2) = 'F*' AND 
             	  		:UsaNemoSVS = 'S')  	 	THEN '013'	-- Dolar Observado	
	        	  WHEN CAR."IdTipoReajuste" = 'USD' THEN '994'	        	                                            	  
	        	  WHEN CAR."IdTipoReajuste" = 'IV'  THEN '997'
	        	  WHEN CAR."IdTipoReajuste" = 'UF'  THEN '998'
	        	  WHEN COALESCE(CAR."IdTipoReajuste",'') IN ('','NR','CLP')  THEN '999'
	         	  ELSE '800'
	         END 																				AS "MonedaNominal"
	         
			,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN
				(CASE WHEN COALESCE(CAR."IdTipoReajuste",'NR') = 'NR'  THEN '1193000'
				      ELSE '1191000' 
				 END)
			     ELSE '0000000'
			 END																				AS "TipoTasaIF20"
			 
			,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN
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
			
			,COALESCE(INS."Tasa", 0) 															AS "TasaEmision"
			,COALESCE(INS."Tera", 0)															AS "Tera"	
			
   		 	 -- Identifica si calcula el campo Valor-Par (WS-R1-CAMPO-23)			
	        ,CASE
	        WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."INGValorRescate"
			ELSE COALESCE(ADV."Valor",'0')
			END    											 									AS "ValorPar"			 
   		 	,TO_VARCHAR( COALESCE( CAR."TasaCompra",0))											AS "TasaCompra25"
			,COALESCE(CAR."MontoCompra", 0)														AS "CostoAdquisicion"
			,case when :TipoCartera = '1' or :TipoCartera = '2'  then 0
			else  COALESCE(CAR."INGTasaMercadoActual", 0) end									AS "TasaCompra27"			
	        ,COALESCE(CAR."INGMontoMercadoActualMonedaInstrumento", 0)							AS "ValorRazonable"
   		 	TO_VARCHAR( COALESCE( ,CAR."INGTasaMercadoActual",0))								AS "TasaValorizacion"	
   		 	
			 ,CASE WHEN ((CAR."IdClasificacionTipoInstrumento" = 'RF') AND 
	        	   		  CAR."IdTipoInstrumento" IN ('CER', 'XER', 'ZER', 'BR')) OR
	        		     (CAR."IdClasificacionTipoInstrumento" = 'IF') THEN 	-- 1 Cero cupon	
	        		CASE WHEN (COALESCE(CAR."INGCantidad", 0) = 0 OR COALESCE(CAR."ValorTipoCambioCierre", 0) = 0) THEN 
	        			0	-- Evita error de division entre 0
	        		ELSE	
			      			CAR."MontoCompra" / (CAR."INGCantidad" * 100 * CAR."ValorTipoCambioCierre")
			      	END	
			  ELSE
			  		COALESCE(CAR."INGPrecioMercadoActual", 0)
			  END																				AS "PrecioCompra"
			 
			,COALESCE(CAR."INGDurationCompraActual", 0)				 							AS "DuracionModificada"  
			,COALESCE(CAR."INGConvexidadCompraActual", 0)										AS "Convexidad"  			 
			,'1'																				AS "CondicionInstrumento" 
	        ,'00000000'																			AS "FechaInicioCond"
	        ,'00000000'																			AS "FechaTermCond"	        
	FROM    "BSCL.Custodia.BD::HIS.Cartera" AS CAR
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = CAR."IdInstrumento"
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS EMI ON EMI."IdEmisor" = INS."IdEmisor"    
    LEFT JOIN "BSCL.Plataforma.BD::INS.ParametroValorizador" PV ON PV."IdOrdenSerie" = INS."IdOrdenSerie"    
    LEFT JOIN "BSCL.Custodia.BD::CUS.CarteraValorAdicionales" ADF ON (ADF."IdCartera" = CAR."IdCartera" AND ADF."IdTipoRegistro" = 'FechaAnteriorCupon') 
    LEFT JOIN "BSCL.Custodia.BD::CUS.CarteraValorAdicionales" ADV ON(ADV."IdCartera" = CAR."IdCartera" AND ADV."IdTipoRegistro" = 'ValorParATasaEmision')
	WHERE CAR."IdEmpresa"						 = :IdEmpresa
	  AND CAR."IdEstado" 						 = 'VIG'
	  AND ((CAR."IdClasificacionTipoInstrumento" IN ('RF','IF') AND :Mercado = '') OR (CAR."IdClasificacionTipoInstrumento" = :Mercado))
	  AND CAR."IdCliente" 			 			 = :IdCliente
	  AND CAR."IdTipoOperacion" 				NOT IN ('COMPAC', 'VENPAC')
	  AND CAR."FechaRespaldo"  					= :Fecha
UNION
		SELECT    
  	         TO_VARCHAR(G."FechaOperacion", 'YYYYMMDD') 										AS "FechaInversion"
	        ,G."IdClasificacionTipoInstrumento"
	        ,G."IdTipoInstrumento"
			,LPAD(COALESCE(CONCAT(SUBSTRING(EMI."DocumentoIdentidad",1,LENGTH(EMI."DocumentoIdentidad")-2),SUBSTRING(EMI."DocumentoIdentidad",LENGTH(EMI."DocumentoIdentidad"),1)),''),10,'0') AS "RutEmisor"
			,CASE 		
				WHEN (G."IdClasificacionTipoInstrumento" = 'IF' AND INS."Nemotecnico" IN ('PDBC','PRBC','PDT') ) THEN '49'
				WHEN G."IdClasificacionTipoInstrumento" = 'IF' AND INS."Nemotecnico" NOT IN ('PDBC','PRBC','PDT')  THEN '10' 	
				WHEN G."IdTipoInstrumento" IN ('BTU','BTP','BCU','BCX','BCP','BCD','ZER', 'CER','XER') THEN '01'
				WHEN G."IdTipoInstrumento" = 'BR'  THEN '03'
				WHEN G."IdTipoInstrumento" = 'LTH' THEN '04'
				WHEN G."IdTipoInstrumento" = 'BEM' THEN '06'				
				WHEN G."IdTipoInstrumento" = 'BBC' THEN '08'
				WHEN (G."IdTipoInstrumento" = 'PAG' OR G."IdTipoInstrumento" = 'PAC') THEN '10'	
			ELSE '49' 	 END																	AS "Familia"		
			      
	        ,CASE WHEN G."IdClasificacionTipoInstrumento" = 'IF' AND UCASE(:UsaNemoSVS) = 'S' 
	        	  THEN COALESCE(G."NemoSVS", CAR."Nemotecnico")
	              ELSE COALESCE(G."Nemotecnico", CAR."Nemotecnico")
	         END 																				AS "Nemo"
	         ,G."Nemotecnico" 																	AS "Nemotecnico" 
	              
	        ,CASE 	WHEN G."IdClasificacionTipoInstrumento" = 'IF' THEN '1'
	        		WHEN  INS."IdTipoInstrumento" = 'LH' THEN '3'
	        		WHEN (INS."NumeroAmortizaciones" = 1 AND  INS."NumeroCupones" = 1) THEN '1'
	        		WHEN (INS."NumeroAmortizaciones" = 1 AND  INS."NumeroCupones" > 1) THEN '2'
	        		ELSE '3'
	        END  AS "TipoRendimiento"							  	
	         	  
	        ,CASE WHEN ((G."IdClasificacionTipoInstrumento" = 'RF') AND 
	        			 G."IdTipoInstrumento" IN ('CER', 'XER', 'ZER', 'BR')) OR
	        		    (G."IdClasificacionTipoInstrumento" = 'IF') OR 
	        		   INS."FrecuenciaCupon" = 0  THEN '0' 													-- 0 Cero cupon
	        	  WHEN INS."FrecuenciaCupon" = 1  THEN '1'		  											-- 1 Mensual
	        	  WHEN INS."FrecuenciaCupon" = 3  THEN '2'													-- 2 Trimestral	
	        	  WHEN INS."FrecuenciaCupon" = 4  THEN '3'		  											-- 3 Cuatrimestral
	        	  WHEN INS."FrecuenciaCupon" = 6  THEN '4'		  											-- 4 Semestral                	  
	        	  WHEN INS."FrecuenciaCupon" = 12 THEN '5'													-- 5 Anual                	  
	         	  ELSE '9' 																					-- 9 Otro
	         END 																				AS "PeriodoCupon"	
	         
	        , COALESCE ( TO_VARCHAR(ADF."Valor", 'YYYYMMDD')  ,'00010101' ) AS "FechaUltimoCupon"
	        , COALESCE ( TO_VARCHAR(ADD_MONTHS  (ADF."Valor",INS."FrecuenciaCupon" )  , 'YYYYMMDD')  ,'00000000' ) 	AS "FechaProximoCupon"
	        
	     	,case when to_INTEGER(TO_VARCHAR(G."INGFechaPagoCorteCupon", 'YYYYMMDD')) > to_INTEGER(TO_VARCHAR(G."FechaVencimientoInstrumento" , 'YYYYMMDD')) 
	     			then COALESCE (TO_VARCHAR(G."INGFechaPagoCorteCupon" , 'YYYYMMDD'),'00010101') else
	        		COALESCE (TO_VARCHAR(G."FechaVencimientoInstrumento" , 'YYYYMMDD'),'00010101') end  AS "FechaVencimientoInstrumento"
	         
	        ,G."IdTipoReajuste"
	        ,CASE WHEN G."IdClasificacionTipoInstrumento" = 'IF' THEN COALESCE(G."INGValorRescate", 0)	
	        ELSE COALESCE(G."INGCantidad", 0)	 END AS   "NominalInicial"
	        
	        ,CASE WHEN G."IdClasificacionTipoInstrumento" = 'IF' THEN COALESCE(G."INGValorRescate", 0)	
	        ELSE COALESCE(G."INGCantidad", 0)	 END AS "NominalActual"

	        ,CASE WHEN G."IdTipoReajuste" = 'EUR' THEN '142'
	        	  WHEN (G."IdTipoReajuste" = 'USD' AND
             	  		G."IdClasificacionTipoInstrumento" = 'IF' AND 
             	  		SUBSTRING(CAR."NemoSVS",1,2) = 'F*' AND 
             	  		:UsaNemoSVS = 'S')  	  THEN '013'	-- Dolar Observado	
	        	  WHEN G."IdTipoReajuste" = 'USD' THEN '994'	        	                                            	  
	        	  WHEN G."IdTipoReajuste" = 'IV'  THEN '997'
	        	  WHEN G."IdTipoReajuste" = 'UF'  THEN '998'
	        	  WHEN COALESCE(G."IdTipoReajuste",'') IN ('','NR','CLP')  THEN '999'
	         	  ELSE '800'
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
			,COALESCE(INS."Tasa", 0) 															AS "TasaEmision"
			,COALESCE(INS."Tera", 0)															AS "Tera"	
			,CASE 
			WHEN G."IdClasificacionTipoInstrumento" = 'IF' THEN G."INGValorRescate"
			ELSE COALESCE(ADV."Valor",'0')
			END    											 									AS "ValorPar"
   		 	,TO_VARCHAR( COALESCE( "TasaCompraOperacionOrigen",0))								AS "TasaCompra25"
			,COALESCE(G."MontoGarantia", 0)														AS "CostoAdquisicion"
			,case when :TipoCartera = '1' or :TipoCartera = '2'  then 0
				else  COALESCE(G."INGTasaMercadoActual", 0) end									AS "TasaCompra27"		
	        ,COALESCE(CAR."INGMontoMercadoActualMonedaInstrumento", 0)							AS "ValorRazonable"
   		 	,TO_VARCHAR( COALESCE( CAR."INGTasaMercadoActual",0)) 								AS "TasaValorizacion" 	
			,CASE WHEN ((G."IdClasificacionTipoInstrumento" = 'RF') AND 
	        			 G."IdTipoInstrumento" IN ('CER', 'XER', 'ZER', 'BR')) OR
		        		(G."IdClasificacionTipoInstrumento" = 'IF') THEN 	-- 1 Cero cupon	
	        	CASE WHEN (COALESCE(G."INGCantidad", 0) = 0 OR COALESCE(CAR."ValorTipoCambioCierre", 0) = 0)  THEN 
	        			0	-- Evita error de division entre 0
	        		ELSE
			    		CAR."MontoCompra" / (G."INGCantidad" * 100 * CAR."ValorTipoCambioCierre")
			    END	
				ELSE
			    	COALESCE(G."INGPrecioMercadoActual", 0)
			 END    											 											AS "PrecioCompra"			 
			 
			,COALESCE(G."INGDurationCompraActual", 0)				 										AS "DuracionModificada"  
			,COALESCE(G."INGConvexidadCompraActual", 0)														AS "Convexidad"  			 
			,'3'																							AS "CondicionInstrumento" 
	        ,CASE WHEN COALESCE(TO_VARCHAR(MovNormal."FechaLiquidacion", 'YYYYMMDD'),'00010101') = '00010101' OR
	        	  	   COALESCE(TO_VARCHAR(MovNormal."FechaLiquidacion", 'YYYYMMDD'),'19000101') = '19000101' THEN '99999999'
	              ELSE
	              	TO_VARCHAR(MovNormal."FechaLiquidacion", 'YYYYMMDD')
	         END																				 			AS "FechaInicioCond"
	         
	        ,CASE WHEN COALESCE(TO_VARCHAR(G."FechaVencimientoOperacion", 'YYYYMMDD'),'00010101') = '00010101' OR
	        	  	   COALESCE(TO_VARCHAR(G."FechaVencimientoOperacion", 'YYYYMMDD'),'19000101') = '19000101' THEN '99999999'
	              ELSE
	              	TO_VARCHAR(G."FechaVencimientoOperacion", 'YYYYMMDD')
	         END	        																				AS "FechaTermCond"
	FROM    "BSCL.Custodia.BD::HIS.Garantia" G
    LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" AS CAR ON (CAR."IdOperacion" = G."IdCarteraOrigen")
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON (INS."IdInstrumento" = G."IdInstrumento")
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS EMI ON EMI."IdEmisor" = INS."IdEmisor"    
    LEFT JOIN "BSCL.Plataforma.BD::INS.ParametroValorizador" PV ON (PV."IdOrdenSerie" = INS."IdOrdenSerie")	
    LEFT JOIN "BSCL.Custodia.BD::CUS.CarteraValorAdicionales" ADF ON (ADF."IdCartera" = CAR."IdCartera" AND ADF."IdTipoRegistro" = 'FechaAnteriorCupon') 
    LEFT JOIN "BSCL.Custodia.BD::CUS.CarteraValorAdicionales" ADV ON(ADV."IdCartera" = CAR."IdCartera" AND ADV."IdTipoRegistro" = 'ValorParATasaEmision')
    LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" as MovNormal 
    	ON 	MovNormal."IdEmpresa" = Car."IdEmpresa" 
	 	AND MovNormal."IdComprobante" = Car."FolioComprobante"
	WHERE G."IdEmpresa"							= :IdEmpresa
	  AND G."IdEstado" 							= 'VIG'
	  AND ((G."IdClasificacionTipoInstrumento" 	IN ('RF','IF') AND :Mercado = '') OR (G."IdClasificacionTipoInstrumento" = :Mercado))
	  AND G."IdCliente" 						= :IdCliente
	  AND CAR."IdTipoOperacion" NOT IN ('COMPAC', 'VENPAC')
	  AND G."FechaRespaldo"  					= :Fecha

UNION
		SELECT    
  	         TO_VARCHAR(CAR."FechaOrigenCompra" , 'YYYYMMDD') 										AS "FechaInversion"
	        ,SUBY."IdClasificacionTipoInstrumento"
	        ,SUBY."IdTipoInstrumento"
			,LPAD(COALESCE(CONCAT(SUBSTRING(EMI."DocumentoIdentidad",1,LENGTH(EMI."DocumentoIdentidad")-2),SUBSTRING(EMI."DocumentoIdentidad",LENGTH(EMI."DocumentoIdentidad"),1)),''),10,'0') AS "RutEmisor"
			,CASE 		
				WHEN (SUBY."IdClasificacionTipoInstrumento" = 'IF' AND INS."Nemotecnico" IN ('PDBC','PRBC','PDT') ) THEN '49'
				WHEN SUBY."IdClasificacionTipoInstrumento" = 'IF' AND INS."Nemotecnico" NOT IN ('PDBC','PRBC','PDT')  THEN '10' 	
				WHEN SUBY."IdTipoInstrumento" IN ('BTU','BTP','BCU','BCX','BCP','BCD','ZER', 'CER','XER') THEN '01'
				WHEN SUBY."IdTipoInstrumento" = 'BR'  THEN '03'
				WHEN SUBY."IdTipoInstrumento" = 'LTH' THEN '04'
				WHEN SUBY."IdTipoInstrumento" = 'BEM' THEN '06'				
				WHEN SUBY."IdTipoInstrumento" = 'BBC' THEN '08'
				WHEN (SUBY."IdTipoInstrumento" = 'PAG' OR SUBY."IdTipoInstrumento" = 'PAC') THEN '10'	
			ELSE '49' 	 END																	AS "Familia"		
			      
	        ,CASE WHEN SUBY."IdClasificacionTipoInstrumento" = 'IF' AND UCASE(:UsaNemoSVS) = 'S' 
	        	  THEN COALESCE(SUBY."NemoSVS", CAR."NemoSVS")
	              ELSE COALESCE(SUBY."Nemotecnico", INS."Nemotecnico")
	         END 																				AS "Nemo"
	         ,SUBY."Nemotecnico" 																	AS "Nemotecnico" 
	              
	        ,CASE 	WHEN SUBY."IdClasificacionTipoInstrumento" = 'IF' THEN '1'
	        		WHEN  INS."IdTipoInstrumento" = 'LH' THEN '3'
	        		WHEN (INS."NumeroAmortizaciones" = 1 AND  INS."NumeroCupones" = 1) THEN '1'
	        		WHEN (INS."NumeroAmortizaciones" = 1 AND  INS."NumeroCupones" > 1) THEN '2'
	        		ELSE '3'
	        END  AS "TipoRendimiento"								  	
	         	  
	        ,CASE WHEN ((SUBY."IdClasificacionTipoInstrumento" = 'RF') AND 
	        			 SUBY."IdTipoInstrumento" IN ('CER', 'XER', 'ZER', 'BR')) OR
	        		    (SUBY."IdClasificacionTipoInstrumento" = 'IF') OR 
	        		   INS."FrecuenciaCupon" = 0  THEN '0' 													-- 0 Cero cupon
	        	  WHEN INS."FrecuenciaCupon" = 1  THEN '1'		  											-- 1 Mensual
	        	  WHEN INS."FrecuenciaCupon" = 3  THEN '2'													-- 2 Trimestral	
	        	  WHEN INS."FrecuenciaCupon" = 4  THEN '3'		  											-- 3 Cuatrimestral
	        	  WHEN INS."FrecuenciaCupon" = 6  THEN '4'		  											-- 4 Semestral                	  
	        	  WHEN INS."FrecuenciaCupon" = 12 THEN '5'													-- 5 Anual                	  
	         	  ELSE '9' 																					-- 9 Otro
	         END 																				AS "PeriodoCupon"	
	
	        , COALESCE ( TO_VARCHAR(ADF."Valor", 'YYYYMMDD')  ,'00010101' ) AS "FechaUltimoCupon"
	        , COALESCE ( TO_VARCHAR(ADD_MONTHS  (ADF."Valor",INS."FrecuenciaCupon" )  , 'YYYYMMDD')  ,'00000000' ) 	AS "FechaProximoCupon"
	        
	     	,case when to_INTEGER(TO_VARCHAR(SUBY."INGFechaPagoCorteCupon", 'YYYYMMDD')) > to_INTEGER(TO_VARCHAR(SUBY."FechaVencimientoInstrumento" , 'YYYYMMDD')) 
	     			then COALESCE (TO_VARCHAR(SUBY."INGFechaPagoCorteCupon" , 'YYYYMMDD'),'00010101') else
	        		COALESCE (TO_VARCHAR(SUBY."FechaVencimientoInstrumento" , 'YYYYMMDD'),'00010101') end  AS "FechaVencimientoInstrumento"

	        ,SUBY."IdTipoReajuste"
			,CASE WHEN G."IdClasificacionTipoInstrumento" = 'IF' THEN COALESCE(G."INGValorRescate", 0)	ELSE COALESCE(G."INGCantidad", 0)	END AS	"NominalInicial"
			,CASE WHEN G."IdClasificacionTipoInstrumento" = 'IF' THEN COALESCE(G."INGValorRescate", 0)	ELSE COALESCE(G."INGCantidad", 0)	END AS	"NominalActual"

	        ,CASE WHEN SUBY."IdTipoReajuste" = 'EUR' THEN '142'
	        	  WHEN (SUBY."IdTipoReajuste" = 'USD' AND
             	  		SUBY."IdClasificacionTipoInstrumento" = 'IF' AND 
             	  		SUBSTRING(SUBY."NemoSVS",1,2) = 'F*' AND 
             	  		:UsaNemoSVS = 'S')  	  	 THEN '013'	-- Dolar Observado	
	        	  WHEN SUBY."IdTipoReajuste" = 'USD' THEN '994'	        	                                            	  
	        	  WHEN SUBY."IdTipoReajuste" = 'IV'  THEN '997'
	        	  WHEN SUBY."IdTipoReajuste" = 'UF'  THEN '998'
	        	  WHEN COALESCE(SUBY."IdTipoReajuste",'') IN ('','NR','CLP')  THEN '999'
	         	  ELSE '800'
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
			
			,COALESCE(INS."Tasa", 0) 				 							 				AS "TasaEmision"
			,COALESCE(INS."Tera", 0)															AS "Tera"				
			,CASE 
			WHEN SUBY."IdClasificacionTipoInstrumento" = 'IF' THEN SUBY."INGValorRescate"
			ELSE COALESCE(ADV."Valor",'0')
			END    											 									AS "ValorPar"
   		 	, TO_VARCHAR( COALESCE( G."TasaCompraOperacionOrigen",0 )) 							AS "TasaCompra25"
			,COALESCE(SUBY."MontoSubyacenteMonedaInstrumento", 0)								AS "CostoAdquisicion"
			,case when :TipoCartera = '1' or :TipoCartera = '2'  then 0
				else  COALESCE(SUBY."INGTasaMercadoActual", 0) end								AS "TasaCompra27"			
	        ,COALESCE(CAR2."INGMontoATasaMercadoActual", 0)										AS "ValorRazonable"
	   		,TO_VARCHAR( COALESCE( CAR."INGTasaMercadoActual",0))								AS "TasaValorizacion"	
   		 	
			,CASE WHEN ((SUBY."IdClasificacionTipoInstrumento" = 'RF') AND 
	        			 SUBY."IdTipoInstrumento" IN ('CER', 'XER', 'ZER', 'BR')) OR
		   	        	(SUBY."IdClasificacionTipoInstrumento" = 'IF') THEN   -- 1 Cero cupon	
		        	CASE WHEN (COALESCE(SUBY."INGCantidad", 0) = 0 OR COALESCE(CAR."ValorTipoCambioCierre", 0) = 0) THEN 
		        			0	-- Evita error de division entre 0
		        		ELSE
				      		CAR."MontoCompra" / (SUBY."INGCantidad" * 100 * CAR."ValorTipoCambioCierre")
				    END	
			    ELSE
			    	COALESCE(SUBY."INGPrecioMercadoActual", 0)
			 END    											 								AS "PrecioCompra"   		 	 
			 
			,COALESCE(SUBY."INGDurationCompraActual", 0)				 						AS "DuracionModificada"  
			,COALESCE(SUBY."INGConvexidadCompraActual", 0)										AS "Convexidad"  			 
			,'2'																				AS "CondicionInstrumento" 
	        ,TO_VARCHAR(SUBY."INGFechaOperacion", 'YYYYMMDD')									AS "FechaInicioCond"
	        ,TO_VARCHAR(CAR2."FechaVencimientoOperacion", 'YYYYMMDD')							AS "FechaTermCond"	        
	FROM    "BSCL.Custodia.BD::HIS.Subyacente" SUBY
	LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" 	AS PAC 	ON (PAC."FolioCartera" = SUBY."FolioCarteraRelacionada"  )
    LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" 	AS CAR 	ON (CAR."IdCartera" = SUBY."IdCarteraRetirada")
    LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" 	AS OPER ON (OPER."IdOperacion" = CAR."IdOperacion")
    INNER JOIN "BSCL.Custodia.BD::HIS.Reserva" 	AS RES 	ON RES."FolioSubyacente"  = SUBY."FolioSubyacente"
	INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" 	AS CAR2 ON (CAR2."IdCartera" = RES."IdCarteraCustodia" )
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON (INS."IdInstrumento" = SUBY."IdInstrumento")
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS EMI ON EMI."IdEmisor" = INS."IdEmisor"    
    LEFT JOIN "BSCL.Plataforma.BD::INS.ParametroValorizador" PV ON (PV."IdOrdenSerie" = INS."IdOrdenSerie")
    LEFT JOIN "BSCL.Custodia.BD::CUS.CarteraValorAdicionales" ADF ON (ADF."IdCartera" = CAR."IdCartera" AND ADF."IdTipoRegistro" = 'FechaAnteriorCupon') 
    LEFT JOIN "BSCL.Custodia.BD::CUS.CarteraValorAdicionales" ADV ON(ADV."IdCartera" = CAR."IdCartera" AND ADV."IdTipoRegistro" = 'ValorParATasaEmision')
	WHERE CAR."IdEmpresa"						= :IdEmpresa
 	AND PAC."Nemotecnico" 						= 'PACTO VENTA'
 	AND CAR."TipoCarteraCorredor" 				<> 'CC'
	AND SUBY."IdEstado" 						= 'VIG'
	AND (SUBY."IdClasificacionTipoInstrumento" <> 'IF' OR SUBY."INGValorRescate" <> 0)
	AND ((SUBY."IdClasificacionTipoInstrumento" IN ('RF','IF') AND :Mercado = '') OR (SUBY."IdClasificacionTipoInstrumento" = :Mercado))
	AND CAR."IdCliente" 						= :IdCliente
	AND SUBY."FechaRespaldo"					= :Fecha
	AND CAR2."FechaRespaldo"					= :Fecha
	AND RES."FechaRespaldo"						= :Fecha
 	