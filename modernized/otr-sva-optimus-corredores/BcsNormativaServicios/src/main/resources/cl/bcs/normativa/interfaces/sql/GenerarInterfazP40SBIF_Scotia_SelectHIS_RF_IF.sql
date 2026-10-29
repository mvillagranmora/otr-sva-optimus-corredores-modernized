 WITH Configuracion_SIGIRP40 AS (-- Parametro para identificar si se calculan los campos Valor Par, Precio Compra, Duracion Modificada y Convexidad	
	SELECT  
		"IdEmpresa"			AS "IdEmpresa",
		"Valor"				AS "SIGIRP40"
	FROM "BSCL.Normativa.BD::NOR.Configuracion"
	WHERE "IdEmpresa"	= :IdEmpresa 	AND "CodigoParametro"	= 'P40SIGIRP40'
),
CONFIGURACION_BANCO_3334 AS (	-- Parametro para identificar si se calculan los campos Duracion Modificada y Convexidad
	SELECT "IdEmpresa"			AS "IdEmpresa",
	       "Valor"				AS "BANCO3334"
	FROM "BSCL.Normativa.BD::NOR.Configuracion"
	WHERE "IdEmpresa"	= :IdEmpresa 	AND "CodigoParametro"	= 'P40BANSCOTIA-33-34' 
),
CONFIGURACION_BANCO_VALIDA AS (	-- Parametro para identificar si se calculan los campos Duracion Modificada y Convexidad
	SELECT "IdEmpresa"			AS "IdEmpresa",
	       "Valor"				AS "BANCOVALIDA"
	FROM "BSCL.Normativa.BD::NOR.Configuracion"
	WHERE "IdEmpresa"	= :IdEmpresa 	AND "CodigoParametro"	= 'P40BSCOTIAVALIDA'
)	
SELECT    -- Consulta Custodia
	CAR."IdCartera",
	TO_VARCHAR(CAR."FechaOperacion", 'YYYYMMDD') 										AS "FechaInversion"
	,CAR."IdClasificacionTipoInstrumento"
	,CAR."IdTipoInstrumento"
	,LPAD(COALESCE(REPLACE(EMI."DocumentoIdentidad", '-','') ,''), 10, '0')	AS "RutEmisor"
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
	      
	,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' AND :UsaNemoSVS = 'S' 
		  THEN COALESCE(CAR."NemoSVS", INS."Nemotecnico")
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

	, COALESCE ( TO_VARCHAR(ADF."Valor", 'YYYYMMDD')  ,'00000000' ) 										AS "FechaUltimoCupon"
	, COALESCE ( TO_VARCHAR(ADD_MONTHS  (ADF."Valor",INS."FrecuenciaCupon" )  , 'YYYYMMDD')  ,'00000000' ) 	AS "FechaProximoCupon"

		,CASE when to_INTEGER(TO_VARCHAR(CAR."INGFechaPagoCorteCupon", 'YYYYMMDD')) > to_INTEGER(TO_VARCHAR(CAR."FechaVencimientoInstrumento" , 'YYYYMMDD')) 
				then COALESCE (TO_VARCHAR(CAR."INGFechaPagoCorteCupon" , 'YYYYMMDD'),'00010101') else
			COALESCE (TO_VARCHAR(CAR."FechaVencimientoInstrumento" , 'YYYYMMDD'),'00010101') 
	 END  																				AS "FechaVencimientoInstrumento"
	 
	,CAR."IdTipoReajuste"


	,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN COALESCE(CAR."INGValorRescate", 0)	
	ELSE COALESCE(CAR."INGCantidad", 0)	 END AS "NominalInicial"
	,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN COALESCE(CAR."INGValorRescate", 0)	
	ELSE COALESCE(CAR."INGCantidad", 0)	 END AS "NominalActual"

	,CASE WHEN CAR."IdTipoReajuste" = 'EUR' THEN '142'
		  WHEN (CAR."IdTipoReajuste" = 'USD' AND
	 	  		CAR."IdClasificacionTipoInstrumento" = 'IF' AND 
	 	  		SUBSTRING(CAR."NemoSVS",1,2) = 'F*' AND 
	 	  		CONFBANCO."BANCOVALIDA" = 'S' AND
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
	,CAST(COALESCE(NULLIF(VPAR."Valor", ''), '0') AS DECIMAL(38,18)) 							AS "ValorPar"

	,LPAD(REPLACE(LPAD(TO_VARCHAR(ABS(CAR."TasaCompra"), '00.0000'), 7, '0'), '.', ''), 6, '0') || CASE WHEN CAR."TasaCompra" < 0 THEN '-' ELSE '+' END AS "TasaCompra25"	 
	,COALESCE(CAR."MontoCompra", 0)														AS "CostoAdquisicion"
	,CASE WHEN :TipoCartera = '1' OR :TipoCartera = '2' THEN 0
	  	  ELSE COALESCE(CAR."INGTasaMercadoActual", 0)
	 END 																				AS "TasaCompra27"
	,COALESCE(CAR."INGMontoATasaMercadoActual", 0)										AS "ValorRazonable"
	,CASE WHEN 
			NULLIF (CAR."INGTasaMercadoActual",0) IS NULL THEN '000000+'
	   ELSE 
	   		LPAD( REPLACE( TO_VARCHAR( ROUND(
	                  CASE WHEN CAR."Nemotecnico" = 'PAGARE NR' 
	                  		THEN ABS (CAR."INGTasaMercadoActual" * 12 )
	                  		ELSE ABS (CAR."INGTasaMercadoActual"      )
	                  END, 4), '00.0000' ), '.', ''), 6, '0') 
	    || CASE WHEN CAR."INGTasaMercadoActual" >= 0 THEN '+' ELSE '-' 
	    END
	 END AS "TasaValorizacion"
	 	 
	 	,COALESCE(CONFSIG."SIGIRP40", 'S')													AS "SIGIRP40"
	 	,COALESCE(CONF3334."BANCO3334", 'S')											AS "BANESTADO3334"
	 	
	 	 -- Identifica si calcula los campos Precio del instrumento (WS-R1-CAMPO-32), 
	 	 -- Duracion Modificada (WS-R1-CAMPO-33) y Convexidad (WS-R1-CAMPO-34)
		,CAR."PrecioCompra" AS "PrecioCompra"
	 
	,	CAR."INGDurationMercadoActual"		AS "DuracionModificada"  
	,  	CAR."INGConvexidadMercadoActual"  	AS "Convexidad" 			 
	,'1'																				AS "CondicionInstrumento" 
	,'00000000'																			AS "FechaInicioCond"
	,'00000000'																			AS "FechaTermCond"
	,INS."Serie" 
	,COALESCE(PFI."Valor",0) AS "Cambio"
FROM "BSCL.Custodia.BD::HIS.Cartera" 						AS CAR 
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 			AS INS 			ON CAR."IdInstrumento" 		= INS."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" 	AS EMI 			ON EMI."IdEmisor" 			= CAR."IdEmisor"    
LEFT JOIN "BSCL.Plataforma.BD::INS.ParametroValorizador" 	AS PV 			ON PV."IdOrdenSerie" 		= INS."IdOrdenSerie"
LEFT JOIN "BSCL.Custodia.BD::CUS.CarteraValorAdicionales" 	AS ADF 			ON (ADF."IdCartera" 		= CAR."IdCartera" AND ADF."IdTipoRegistro" = 'FechaAnteriorCupon' and ADF."Fecha" = :Fecha) 
LEFT JOIN "BSCL.Custodia.BD::CUS.CarteraValorAdicionales"	AS 	VPAR 		ON 	(VPAR."IdCartera" 		= CAR."IdCartera" AND VPAR."IdTipoRegistro" = 'ValorParATasaEmision' AND VPAR."Fecha" =:Fecha ) 
LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" 	AS PFI			ON (CAR."IdTipoReajuste" 	= PFI."IdParametroFinanciero" AND PFI."Fecha" =:Fecha )
LEFT JOIN Configuracion_SIGIRP40 							AS CONFSIG 		ON CONFSIG."IdEmpresa" 		= CAR."IdEmpresa"
LEFT JOIN Configuracion_BANCO_3334 							AS CONF3334 	ON CONF3334."IdEmpresa" 	= CAR."IdEmpresa"
LEFT JOIN Configuracion_BANCO_VALIDA 						AS CONFBANCO 	ON CONFBANCO."IdEmpresa" 	= CAR."IdEmpresa"
WHERE CAR."IdEmpresa"						= 			:IdEmpresa
AND CAR."IdEstado" 						 	= 			'VIG'
AND ((CAR."IdClasificacionTipoInstrumento" IN ('RF','IF') AND :Mercado = '') OR (CAR."IdClasificacionTipoInstrumento" = :Mercado))
AND CAR."IdCliente" 						= 			:IdCliente
AND  CAR."Nemotecnico" 						not like 	'PACTO%'
AND CAR."FechaRespaldo"						= 			:Fecha

	UNION all

SELECT    -- Consulta Garantias
	G."IdCarteraOrigen" AS "IdCartera",
	TO_VARCHAR(G."FechaOperacion", 'YYYYMMDD') 										AS "FechaInversion"
    ,G."IdClasificacionTipoInstrumento"
    ,G."IdTipoInstrumento"
    ,LPAD(COALESCE(REPLACE(EMI."DocumentoIdentidad", '-','') ,''), 10, '0')	AS "RutEmisor"        
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
    ,CASE WHEN G."IdClasificacionTipoInstrumento" = 'IF' AND :UsaNemoSVS = 'S' 
    	  THEN COALESCE(G."NemoSVS", CAR."Nemotecnico")
          ELSE COALESCE(G."Nemotecnico", CAR."Nemotecnico")
     END  																				AS "Nemo"
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
	, COALESCE ( TO_VARCHAR(ADF."Valor", 'YYYYMMDD')  ,'00000000' ) 										AS "FechaUltimoCupon"
    , COALESCE ( TO_VARCHAR(ADD_MONTHS  (ADF."Valor",INS."FrecuenciaCupon" )  , 'YYYYMMDD')  ,'00000000' ) 	AS "FechaProximoCupon"
 	,CASE when to_INTEGER(TO_VARCHAR(G."INGFechaPagoCorteCupon", 'YYYYMMDD')) > to_INTEGER(TO_VARCHAR(G."FechaVencimientoInstrumento" , 'YYYYMMDD')) 
 			then COALESCE (TO_VARCHAR(G."INGFechaPagoCorteCupon" , 'YYYYMMDD'),'00010101') else
    		COALESCE (TO_VARCHAR(G."FechaVencimientoInstrumento" , 'YYYYMMDD'),'00010101') 
     END  AS "FechaVencimientoInstrumento"
    ,G."IdTipoReajuste"
    ,CASE WHEN G."IdClasificacionTipoInstrumento" = 'IF' THEN COALESCE(G."INGValorRescate", 0)	
    ELSE COALESCE(G."INGCantidad", 0)	 END AS   "NominalInicial"
    
    ,CASE WHEN G."IdClasificacionTipoInstrumento" = 'IF' THEN COALESCE(G."INGValorRescate", 0)	
    ELSE COALESCE(G."INGCantidad", 0)	 END AS "NominalActual"
												   
    ,CASE WHEN G."IdTipoReajuste" = 'EUR' THEN '142'
    	  WHEN (G."IdTipoReajuste" = 'USD' AND
     	  		G."IdClasificacionTipoInstrumento" = 'IF' AND 
     	  		SUBSTRING(CAR."NemoSVS",1,2) = 'F*' AND 
     	  		CONFBANCO."BANCOVALIDA" = 'S' AND
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
	,CAST(COALESCE(NULLIF(VPAR."Valor", ''), '0') AS DECIMAL(38,18)) 							AS "ValorPar"
	,LPAD(REPLACE(LPAD(TO_VARCHAR(ABS(G."TasaCompraOperacionOrigen"), '00.0000'), 7, '0'), '.', ''), 6, '0') || CASE WHEN G."TasaCompraOperacionOrigen" < 0 THEN '-' ELSE '+' END AS "TasaCompra25"
	,COALESCE( ROUND (   G."Cantidad" / CAR."Cantidad" *  CAR."MontoCompra" ,0 ) , 0)														AS "CostoAdquisicion"
	,CASE WHEN :TipoCartera = '1' OR :TipoCartera = '2' 
			THEN 0
	  	  	ELSE COALESCE(G."INGTasaMercadoActual", 0)
	 END 																				AS "TasaCompra27"
    ,COALESCE(G."INGMontoATasaMercadoActual",0)											AS "ValorRazonable"	
    ,CASE WHEN COALESCE(NULLIF(CAR."INGTasaMercadoActual", 0), NULLIF(G."INGTasaMercadoActual", 0)) IS NULL THEN '000000+'
     ELSE  LPAD(REPLACE(TO_VARCHAR(ROUND(ABS(
          	CASE WHEN G."Nemotecnico" = 'PAGARE NR' 
            	THEN ABS (COALESCE(CAR."INGTasaMercadoActual", G."INGTasaMercadoActual") * 12 )
            	ELSE ABS (COALESCE(CAR."INGTasaMercadoActual", G."INGTasaMercadoActual")      )
        	END
       		), 4), '00.0000'), '.', ''), 6, '0') 
        || CASE WHEN COALESCE(CAR."INGTasaMercadoActual", G."INGTasaMercadoActual") >= 0 THEN '+' ELSE '-' END
 	 END 																				AS "TasaValorizacion"
	,COALESCE(CONFSIG."SIGIRP40", 'S')													AS "SIGIRP40"
	 	,COALESCE(CONF3334."BANCO3334", 'S')											AS "BANESTADO3334"
	 	
	 	 -- Identifica si calcula los campos Precio Compra (WS-R1-CAMPO-32), 
	 	 -- Duracion Modificada (WS-R1-CAMPO-33) y Convexidad (WS-R1-CAMPO-34)
	,CAR."PrecioCompra"	AS "PrecioCompra"    		 	 
	,	CAR."INGDurationMercadoActual"		AS "DuracionModificada"  
	,  	CAR."INGConvexidadMercadoActual"  	AS "Convexidad"			 
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
     ,INS."Serie" 
    ,PFI."Valor" AS "Cambio"
FROM    "BSCL.Custodia.BD::HIS.Garantia" 						AS 	G
LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" 						AS 	CAR  		ON CAR."IdCartera" 			= G."IdCarteraOrigen" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 	AS 	CLI 		ON (CLI."IdEmpresa" 		= CAR."IdEmpresa" 	AND CLI."IdCliente"			= CAR."IdCliente")    
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 				AS 	INS 		ON (INS."IdInstrumento" 	= G."IdInstrumento")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" 		AS 	EMI 		ON EMI."IdEmisor" 			= CAR."IdEmisor"    
LEFT JOIN "BSCL.Plataforma.BD::INS.ParametroValorizador" 		AS 	PV 			ON (PV."IdOrdenSerie" 		= INS."IdOrdenSerie")	
LEFT JOIN "BSCL.Custodia.BD::CUS.CarteraValorAdicionales" 		AS 	ADF 		ON (ADF."IdCartera" 		= CAR."IdCartera" 	AND ADF."IdTipoRegistro" 	= 'FechaAnteriorCupon'and ADF."Fecha" = :Fecha)  
LEFT JOIN "BSCL.Custodia.BD::CUS.CarteraValorAdicionales"		AS 	VPAR 		ON 	(VPAR."IdCartera" 		= CAR."IdCartera" AND VPAR."IdTipoRegistro" = 'ValorParATasaEmision' AND VPAR."Fecha" =:Fecha ) 
LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" 		AS	PFI			ON (CAR."IdTipoReajuste" 	= PFI."IdParametroFinanciero" AND PFI."Fecha" 	=:Fecha )
LEFT JOIN Configuracion_SIGIRP40 								AS 	CONFSIG 	ON CONFSIG."IdEmpresa" 		= G."IdEmpresa"
LEFT JOIN Configuracion_BANCO_3334 								AS	CONF3334	ON CONF3334."IdEmpresa" 	= G."IdEmpresa"
LEFT JOIN Configuracion_BANCO_VALIDA							AS 	CONFBANCO 	ON CONFBANCO."IdEmpresa" 	= G."IdEmpresa"
WHERE G."IdEmpresa"							= :IdEmpresa
AND G."IdEstado" 							= 'VIG'
AND ((G."IdClasificacionTipoInstrumento" 	IN ('RF','IF') AND :Mercado = '') OR (G."IdClasificacionTipoInstrumento" = :Mercado))
AND G."IdCliente" 							= :IdCliente
AND CAR."IdTipoOperacion" 					NOT IN ('COMPAC', 'VENPAC')
AND G."FechaRespaldo"						= :Fecha
AND CAR."FechaRespaldo"  					= :Fecha

	UNION all

SELECT   -- CPNSULTA DE PACTOS
		RES."IdCarteraCustodia" AS "IdCartera",
		CASE WHEN CAR."FechaOrigenCompra" <> '0001-01-01'
		THEN  TO_VARCHAR(CAR."FechaOrigenCompra" , 'YYYYMMDD') 
		ELSE  TO_VARCHAR(CAR."FechaOperacion" , 'YYYYMMDD')  END AS "FechaInversion"
        ,SUBY."IdClasificacionTipoInstrumento"
        ,SUBY."IdTipoInstrumento"
        ,LPAD(COALESCE(REPLACE(EMI."DocumentoIdentidad", '-','') ,''), 10, '0')	AS "RutEmisor"        
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
         
        ,CASE WHEN SUBY."IdClasificacionTipoInstrumento" = 'IF' AND :UsaNemoSVS = 'S' 
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

        , COALESCE ( TO_VARCHAR(ADF."Valor", 'YYYYMMDD')  ,'00000000' ) 										AS "FechaUltimoCupon"
        , COALESCE ( TO_VARCHAR(ADD_MONTHS  (ADF."Valor",INS."FrecuenciaCupon" )  , 'YYYYMMDD')  ,'00000000' ) 	AS "FechaProximoCupon"
        
     	,case when to_INTEGER(TO_VARCHAR(SUBY."INGFechaPagoCorteCupon", 'YYYYMMDD')) > to_INTEGER(TO_VARCHAR(SUBY."FechaVencimientoInstrumento" , 'YYYYMMDD')) 
     			then COALESCE (TO_VARCHAR(SUBY."INGFechaPagoCorteCupon" , 'YYYYMMDD'),'00010101') else
        		COALESCE (TO_VARCHAR(SUBY."FechaVencimientoInstrumento" , 'YYYYMMDD'),'00010101') end  AS "FechaVencimientoInstrumento"
         
        ,SUBY."IdTipoReajuste"
        ,CASE WHEN CAR2."IdCartera" IS NULL THEN  SUBY."INGValorRescate"
        WHEN SUBY."IdClasificacionTipoInstrumento" = 'IF' THEN COALESCE(SUBY."MontoSubyacente", 0)	
        ELSE COALESCE(CAR2."INGCantidad", 0)	 END AS "NominalInicial"
        ,CASE WHEN CAR2."IdCartera" IS NULL THEN  SUBY."INGValorRescate"
        WHEN SUBY."IdClasificacionTipoInstrumento" = 'IF' THEN COALESCE(SUBY."MontoSubyacente", 0)	
        ELSE COALESCE(CAR2."INGCantidad", 0)	 END AS "NominalActual"
        
        ,CASE WHEN SUBY."IdTipoReajuste" = 'EUR' THEN '142'
        	  WHEN (SUBY."IdTipoReajuste" = 'USD' AND
         	  		SUBY."IdClasificacionTipoInstrumento" = 'IF' AND 
         	  		SUBSTRING(SUBY."NemoSVS",1,2) = 'F*' AND 
         	  		CONFBANCO."BANCOVALIDA" = 'S' AND
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
		
		,COALESCE(INS."Tasa", 0) 															AS "TasaEmision"
		,COALESCE(INS."Tera", 0)															AS "Tera"	
		
		,CAST(COALESCE(NULLIF(VPAR."Valor", ''), '0') AS DECIMAL(38,18)) 							AS "ValorPar"	 
		 	,CASE WHEN (NULLIF (CAR."INGTasaCompra",0) IS NOT null ) THEN 
			LPAD(REPLACE(LPAD(TO_VARCHAR(ABS(CAR."INGTasaCompra"), '00.0000'), 7, '0'), '.', ''), 6, '0') || CASE WHEN CAR."INGTasaCompra" < 0 THEN '-' ELSE '+' END
		WHEN (NULLIF (RES."Tasa",0) IS NOT NULL) THEN 
			LPAD(REPLACE(LPAD(TO_VARCHAR(ABS(RES."Tasa"), '00.0000'), 7, '0'), '.', ''), 6, '0') || CASE WHEN RES."Tasa" < 0 THEN '-' ELSE '+' END
		WHEN (NULLIF (CAR2."TasaCompra",0) IS NOT NULL ) THEN 
			LPAD(REPLACE(LPAD(TO_VARCHAR(ABS(CAR2."TasaCompra"), '00.0000'), 7, '0'), '.', ''), 6, '0') || CASE WHEN CAR2."TasaCompra" < 0 THEN '-' ELSE '+' END
		ELSE '000000+' 	END 											AS "TasaCompra25"
		 	 
		,COALESCE(SUBY."MontoSubyacenteMonedaInstrumento" , 0)												AS "CostoAdquisicion"

		,CASE WHEN :TipoCartera = '1' OR :TipoCartera = '2' THEN 0
		  	  ELSE COALESCE(SUBY."INGTasaMercadoActual", 0)
		 END 																				AS "TasaCompra27"
		 
        ,COALESCE( NULLIF(CAR2."INGMontoATasaMercadoActual",0) , NULLIF(CAR."INGMontoATasaCompraActual",0) , 0)								AS "ValorRazonable"
		,CASE WHEN 
				NULLIF (COALESCE (CAR2."INGTasaMercadoActual",SUBY."INGTasaMercadoActual"),0) IS NULL THEN '000000+'
		   ELSE 
		   		LPAD( REPLACE( TO_VARCHAR( ROUND(
		                  CASE WHEN CAR."Nemotecnico" = 'PAGARE NR' 
		              		THEN ABS ( COALESCE ( CAR2."INGTasaMercadoActual",SUBY."INGTasaMercadoActual") * 12 )
		              		ELSE ABS ( COALESCE ( CAR2."INGTasaMercadoActual",SUBY."INGTasaMercadoActual")      )
		              END, 4), '00.0000' ), '.', ''), 6, '0') 
			|| CASE WHEN COALESCE ( CAR2."INGTasaMercadoActual",SUBY."INGTasaMercadoActual") >= 0 THEN '+' ELSE '-' 
		    END
		 END AS "TasaValorizacion"
		 	 
		 	,COALESCE(CONFSIG."SIGIRP40", 'S')													AS "SIGIRP40"
		 	,COALESCE(CONF3334."BANCO3334", 'S')											AS "BANESTADO3334"
		 	
		 	 -- Identifica si calcula los campos Precio Compra (WS-R1-CAMPO-32), 
		 	 -- Duracion Modificada (WS-R1-CAMPO-33) y Convexidad (WS-R1-CAMPO-34)
		,CAR."PrecioCompra" AS "PrecioCompra"  		 	 
		,	SUBY."INGDurationMercadoActual" 		AS 	"DuracionModificada"  
		,  	SUBY."INGConvexidadMercadoActual"  		AS 	"Convexidad"		 
		,'2'																				AS "CondicionInstrumento" 
        ,TO_VARCHAR(SUBY."INGFechaOperacion", 'YYYYMMDD')									AS "FechaInicioCond"
        ,TO_VARCHAR(PAC."FechaVencimientoOperacion" , 'YYYYMMDD')	AS "FechaTermCond"	      
        ,INS."Serie" 
        ,PFI."Valor" AS "Cambio"
        
FROM    "BSCL.Custodia.BD::HIS.Subyacente" SUBY
INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" 					AS 	PAC 		ON 	(PAC."FolioCartera" 	= SUBY."FolioCarteraRelacionada"  )
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" 					AS 	CAR 		ON 	(CAR."IdCartera" 		= SUBY."IdCarteraRetirada" )		-- Cabecera del pacto
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" 					AS 	RES 		ON 	RES."FolioSubyacente"  = SUBY."FolioSubyacente"
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" 					AS 	CAR2 		ON 	(CAR2."IdCartera" 		= RES."IdCarteraCustodia" )			-- Cartera Detalle
LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" 					AS 	OPER 		ON 	(OPER."IdOperacion" 	= CAR."IdOperacion")
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 			AS 	INS			ON 	(INS."IdInstrumento" 	= SUBY."IdInstrumento")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" 	AS 	EMI 		ON 	(EMI."IdEmisor" 		= SUBY."IdEmisor"    )
LEFT JOIN "BSCL.Plataforma.BD::INS.ParametroValorizador" 	AS 	PV 			ON 	(PV."IdOrdenSerie" 		= INS."IdOrdenSerie")
LEFT JOIN "BSCL.Custodia.BD::CUS.CarteraValorAdicionales" 	AS 	ADF 		ON 	(ADF."IdCartera" 		= RES."IdCarteraCustodia" 	AND ADF."IdTipoRegistro" 	= 'FechaAnteriorCupon' 		and ADF."Fecha" = :Fecha) 
LEFT JOIN "BSCL.Custodia.BD::CUS.CarteraValorAdicionales"	AS 	VPAR 		ON 	(VPAR."IdCartera" 		= RES."IdCarteraCustodia" 	AND VPAR."IdTipoRegistro" 	= 'ValorParATasaEmision' AND VPAR."Fecha" =:Fecha  ) 
LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero"		AS 	PFI			ON 	(CAR."IdTipoReajuste" 	= PFI."IdParametroFinanciero" AND PFI."Fecha" =:Fecha )
LEFT JOIN Configuracion_SIGIRP40 							AS 	CONFSIG 	ON 	CONFSIG."IdEmpresa" 	= CAR."IdEmpresa"
LEFT JOIN Configuracion_BANCO_3334 							AS	CONF3334	ON 	CONF3334."IdEmpresa" 	= OPER."IdEmpresa"
LEFT JOIN Configuracion_BANCO_VALIDA						AS 	CONFBANCO 	ON 	CONFBANCO."IdEmpresa" 	= OPER."IdEmpresa"
WHERE CAR."IdEmpresa"						= :IdEmpresa
AND PAC."Nemotecnico" 						= 'PACTO VENTA'
AND SUBY."IdEstado" 						IN ('VIG' , 'RPA')
AND (SUBY."IdClasificacionTipoInstrumento" 	<> 'IF' OR SUBY."INGValorRescate" <> 0)
AND ((SUBY."IdClasificacionTipoInstrumento" IN ('RF','IF') AND :Mercado = '') OR (SUBY."IdClasificacionTipoInstrumento" = :Mercado))
AND (CAR."TipoCarteraCorredor" 				<> 'CC' AND CAR."IdCliente" 						= :IdCliente)  
AND SUBY."FechaRespaldo" 					= :Fecha
AND PAC."FechaRespaldo"  					= :Fecha
 