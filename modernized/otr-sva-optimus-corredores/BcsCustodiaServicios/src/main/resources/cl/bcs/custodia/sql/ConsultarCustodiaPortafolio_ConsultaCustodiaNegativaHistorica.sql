WITH custodia AS (
	SELECT
       CLASINST."IdClasificacionTipoInstrumento"
       ,inst3."IdTipoInstrumento" 
       ,CAR."IdInstrumento"
       ,'' AS "EsCustodiaExterna"
       ,CASE 
       		WHEN CLASINST."IdClasificacionTipoInstrumento" = 'IF' 
       			THEN CAR."NemoSVS" 
       			ELSE CAR."Nemotecnico" 
       		END	 														AS "Nemotecnico"
       ,CASE 
       		WHEN COALESCE(CAR."IdEmisor",inst3."IdEmisor",'') <> '' 
       			THEN COALESCE(CAR."IdEmisor", inst3."IdEmisor",'') 
       			ELSE inst3."IdEmisor" 
       		END 														AS "IdEmisor"
       ,CASE 
       		WHEN CLASINST."IdClasificacionTipoInstrumento" = 'IF' 
       			THEN CAR."EsFungible" ELSE 'N' 
       		END 														AS "EsFungible"
       ,CAR."IdCliente"
       ,CLI."IdPersona"
       ,CAR."IdPortafolio"
       ,COALESCE(MADOR."NombreProductoAgrupador", 'Otros (No APV)')		AS "Producto"
       ,CASE 
       		WHEN :p_AgrupaPorBoveda='N' 
       			THEN :v_IdBovedaDefecto 
       			ELSE COALESCE(CAR."IdBoveda",:v_IdBovedaDefecto) 
       		END 														AS "IdBoveda" 
       ,CASE 
       		WHEN :p_AgrupaPorBoveda='N' 
       			THEN  0 
       			ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio", 0) 
       		END 														AS "IdCuentaCustodioPortafolio"
       ,CASE 
       		WHEN :p_AgrupaPorBoveda='N' 
       			THEN '' ELSE COALESCE(CCPI."Codigo", '') 
       		END 														AS "NumeroCuentaBoveda" 
       ,CAST(IFNULL(
       		CASE WHEN CLASINST."IdClasificacionTipoInstrumento" = 'IF' 
       			THEN  CAR."ValorRescate" 
       			ELSE  CAR."Cantidad"
       	END,0) AS DECIMAL(26,8)) 										AS "Cantidad"
       ,COALESCE (CAR."PrecioOperacion" ,0) 								AS "PrecioCompra"
       ,COALESCE (CAR."MontoOperacionMonedaInstrumento" ,0) 					AS "MontoCompra"                   
       ,COALESCE (CAR."MontoOperacionMonedaInstrumento" ,0) 		AS "ValorCompra"       
       ,COALESCE (CAR."PrecioMercado" ,0) 						AS "INGPrecioMercadoActual"           
       ,COALESCE (CAR."TasaMercado" ,0) 						AS "INGTasaMercadoActual"           
       ,COALESCE (CAR."MontoMercadoMonedaInstrumento" ,0) 		AS "ValorMercado"
       ,COALESCE (CAR."MontoMercadoMonedaInstrumento" ,0) 		AS "MontoMercado"
       ,CASE
            WHEN COALESCE(CAR."IdTipoReajuste",inst3."IdTipoReajuste", '') <> '' 
            	THEN COALESCE(CAR."IdTipoReajuste",inst3."IdTipoReajuste", '')
            WHEN ifnull(CAR."IdMonedaInstrumento" ,'') <> '' 
            	THEN CAR."IdMonedaInstrumento" 
            ELSE COALESCE(CAR."IdMonedaInstrumento",inst3."IdMoneda",'')
        END 															AS "IdMonedaInstrReajuste"
       ,CASE
            WHEN IFNULL(CAR."IdMonedaEmision",'') <> '' 
            	THEN CAR."IdMonedaEmision"
            WHEN IFNULL(CAR."IdTipoReajuste",'') <> '' 
            	THEN CAR."IdTipoReajuste"
            ELSE inst3."IdMoneda"
        END 															AS "IdMonedaEmision"
       ,CASE 
            WHEN IFNULL(CAR."IdMonedaInstrumento",'') <> '' 
            	THEN IFNULL(CAR."IdMonedaInstrumento",'')
            WHEN COALESCE(CAR."IdTipoReajuste",inst3."IdTipoReajuste", '') <> '' 
            	THEN COALESCE(CAR."IdTipoReajuste",inst3."IdTipoReajuste", '')
            ELSE COALESCE(CAR."IdMonedaInstrumento",inst3."IdMoneda",'')
        END 															AS "IdMonedaPrincipal"
        ,CASE 
        	WHEN IFNULL(CAR."IdMonedaInstrumento",'') = 'UF'
        		THEN 'CLP'
        	ELSE 
        		CAR."IdMonedaInstrumento"
        END         AS "EsPagaderoEn"
       ,CAST(0 AS  DECIMAL(26,8)) AS "InteresCorrido"
	   ,'' 		AS "RegimenTributario"
       ,CAST(0 AS DECIMAL(26,8))  		AS "INGPrecioPromedioCompra"
       ,CAST(0 AS DECIMAL(26,8)) 		AS "INGMontoPrecioPromedioMonedaInstrumento"
       , '' 							AS "IdMonedaSistema"
		,'' 						AS "IdRegimenTributario"
       ,CASE 
       		WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 
       			THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) 
       		ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) 
       	END 															AS "IdMatrizAgrupador"
 	   ,CASE 
 	   		WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 
 	   			THEN COALESCE(MatrizAgrupador."EsADC",'N') 
 	   		ELSE COALESCE(MAT_NOAPV."EsADC",'N') 
 	   	END 															AS "EsADC"
       , CAR."IdTipoOperacion"
     	, CAST(0 AS DECIMAL(26,8))  AS "ValorFactorMonedaInstrumento"
 	   , inst3."RazonSocial" AS "RazonSocialInstrumento"
 	   , '' AS "CodigoRegimenTipoAhorro"
        , CAR."FechaOperacion" as "INGFechaOperacion"
        , CAR."FechaLiquidacion" AS "INGFechaLiquidacion"
        , CAR."IdEmpresa"
    FROM "BSCL.Custodia.BD::HIS.CarteraNegativa" 					AS CAR      
    LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" 			AS CLI 				ON (CLI."IdEmpresa" = 1 AND CLI."IdCliente" = CAR."IdCliente")  
    LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio"			AS Portafolio	 	ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
    LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" 	AS MatrizAgrupador 	ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
    LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI 		ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 		AS inst3 			ON inst3."IdInstrumento" = CAR."IdInstrumento"
    LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV"AS MAT_NOAPV 		ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
    LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" 			AS MADOR			ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = INST3."IdTipoInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON ClasInst."IdClasificacionTipoInstrumento" = TipoInstrumento."IdClasificacionTipoInstrumento" 
    
    WHERE 

CAR."IdEmpresa" = :p_IdEmpresa
AND CAR."IdEstado" = 'VIG'
AND CAR."FechaRespaldo" = :v_FechaConsulta
<<CASE.CAR.p_IdCliente>>
<<CASE.CAR.p_IdPortafolio>>
<<CASE.CLASINST.p_IdClasificacionTipoInstrumento>>
<<CASE.TipoInstrumento.p_IdTipoInstrumento>> 
<<CASE.CAR.p_IdInstrumento>>
<<CASE.CAR.p_Nemotecnico>>
<<COALESCE.MatrizAgrupador.p_IdProductoAgrupador>>
)
SELECT
    "IdClasificacionTipoInstrumento"
   ,"IdTipoInstrumento"
   ,"IdInstrumento"
   ,"Nemotecnico"
   ,"IdEmisor"
   ,"EsFungible"
   ,"IdCliente"
   ,"IdPortafolio"
   ,"Producto"
   ,"IdBoveda"
   ,"IdCuentaCustodioPortafolio"
   ,"NumeroCuentaBoveda"
   ,"Cantidad"
   ,"PrecioCompra"
   ,"ValorCompra"
   ,"MontoCompra"
   ,"INGPrecioMercadoActual"
   ,"INGTasaMercadoActual"
   ,"ValorMercado"
   ,"MontoMercado"
   ,"IdMonedaInstrReajuste"
   ,"IdMonedaEmision"
   ,"IdMonedaPrincipal"
   ,"InteresCorrido"
   ,"RegimenTributario"
   ,"INGPrecioPromedioCompra"
   ,"IdRegimenTributario"
   ,"IdMatrizAgrupador"
   ,"EsADC"
   ,"IdPersona"
   ,"IdTipoOperacion"
   ,"ValorFactorMonedaInstrumento"
   ,"RazonSocialInstrumento"
   , "EsCustodiaExterna"
   , "EsPagaderoEn"
   , "CodigoRegimenTipoAhorro"
   , 'S' AS "EsNegativa"
FROM custodia
where custodia."IdEmpresa" = :p_IdEmpresa <<FECHACONSULTA.custodia.p_TipoFechaConsulta>>
    
