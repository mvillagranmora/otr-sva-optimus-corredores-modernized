
WITH resultadoTotal AS (
	SELECT 	RES."FolioCarteraOrigen"
			,SUM("IngCantidad") 									AS "IngCantidad"
			,SUM("IngValorRescate") 								AS "IngValorRescate"
			,SUM("MontoValorizado") 								AS "IngMontoValorizado"
	
	FROM "BSCL.Custodia.BD::CUS.Reserva" 							AS RES 
	
	WHERE 	RES."IdEstado"='VIG' 
		AND RES."EgrCantidad" = 0
	
	GROUP BY RES."FolioCarteraOrigen"
	
), custodia AS (
	SELECT
        CAR."IdClasificacionTipoInstrumento"
       ,CAR."IdTipoInstrumento"
       ,CAR."IdInstrumento"
       ,CAR."EsCustodiaExterna" AS "EsCustodiaExterna"
       ,CASE 
       		WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' 
       			THEN CAR."NemoSVS" 
       			ELSE CAR."Nemotecnico" 
       		END	 														AS "Nemotecnico"
       ,CASE 
       		WHEN COALESCE(CAR."IdEmisor",inst3."IdEmisor",'') <> '' 
       			THEN COALESCE(CAR."IdEmisor",inst3."IdEmisor",'') 
       			ELSE inst3."IdEmisor" 
       		END 														AS "IdEmisor"
       ,CASE 
       		WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' 
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
       			ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio",0) 
       		END 														AS "IdCuentaCustodioPortafolio"
       ,CASE 
       		WHEN :p_AgrupaPorBoveda='N' 
       			THEN '' ELSE COALESCE(CCPI."Codigo",'') 
       		END 														AS "NumeroCuentaBoveda" 
       ,CAST(IFNULL(
       		CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' 
       			THEN  CAR."INGValorRescate" - COALESCE (resTot."IngValorRescate",0)
       			ELSE  CAR."INGCantidad"- COALESCE (resTot."IngCantidad",0)  
       	END,0) AS DECIMAL(26,8)) 										AS "Cantidad"
       ,COALESCE (CAR."PrecioCompra",0) 								AS "PrecioCompra"
       ,COALESCE (CAR."INGMontoATasaCompraActual",0) 					AS "MontoCompra"                   
       ,COALESCE (CAR."INGMontoCompraActualMonedaInstrumento",0) 		AS "ValorCompra"       
       ,COALESCE (CAR."INGPrecioMercadoActual",0) 						AS "INGPrecioMercadoActual"           
       ,COALESCE (CAR."INGTasaMercadoActual",0) 						AS "INGTasaMercadoActual"           
       ,COALESCE (CAR."INGMontoMercadoActualMonedaInstrumento",0) 		AS "ValorMercado"
       ,COALESCE (CAR."INGMontoATasaMercadoActual",0) 					AS "MontoMercado"
       ,CASE
            WHEN COALESCE(CAR."IdTipoReajuste",inst3."IdTipoReajuste", '') <> '' 
            	THEN COALESCE(CAR."IdTipoReajuste",inst3."IdTipoReajuste", '')
            WHEN ifnull(CAR."IdMonedaPrincipal",'') <> '' 
            	THEN CAR."IdMonedaPrincipal"
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
            WHEN IFNULL(CAR."IdMonedaPrincipal",'') <> '' 
            	THEN IFNULL(CAR."IdMonedaPrincipal",'')
            WHEN COALESCE(CAR."IdTipoReajuste",inst3."IdTipoReajuste", '') <> '' 
            	THEN COALESCE(CAR."IdTipoReajuste",inst3."IdTipoReajuste", '')
            ELSE COALESCE(CAR."IdMonedaInstrumento",inst3."IdMoneda",'')
        END 															AS "IdMonedaPrincipal"
        ,CASE 
        	WHEN IFNULL(CAR."IdMonedaInstrumento",'') = 'UF'
        		THEN 'CLP'
        	ELSE 
        		CAR."IdMonedaInstrumento"
        END          AS "EsPagaderoEn"   -- Moneda de Pago  
       ,COALESCE (CAR."INGMontoInteresCorridoCompraActual",0) 			AS "InteresCorrido"
       ,COALESCE (REG."Descripcion",'') 								AS "RegimenTributario"
       ,COALESCE (CAR."INGPrecioPromedioCompra",0) 						AS "INGPrecioPromedioCompra"
       ,COALESCE (CAR."INGMontoPrecioPromedioMonedaInstrumento",0) 		AS "INGMontoPrecioPromedioMonedaInstrumento"
       ,COALESCE (CAR."IdMonedaSistema",'') 							AS "IdMonedaSistema"
       ,COALESCE (REG."IdRegimenFondoMutuo",'') 						AS "IdRegimenTributario"
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
       ,CAR."IdTipoOperacion"
       ,CAR."ValorFactorMonedaInstrumento"
 	   ,inst3."RazonSocial" AS "RazonSocialInstrumento"
 	   , COALESCE(REGTRIB."CodigoRegimenTipoAhorro", '') AS "CodigoRegimenTipoAhorro"
 	   , inst3."CobroComisionesCustodia" AS "CobroComisionesCustodia"
 	   , COALESCE(CIP."ValorIndicador",'0') AS "OpcionCobro"
 	   , CASE 
  			WHEN CAR."IdClasificacionTipoInstrumento" = 'RF' THEN
  				COALESCE(((CAR."INGCantidad" * CAR."MontoCompra") / NULLIF(CAR."Cantidad",0)),0)
  			WHEN CAR."IdClasificacionTipoInstrumento" = 'RV' THEN
  				(CAR."INGCantidad" * CAR."PrecioCompra")
  			WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN
  				COALESCE(((CAR."INGValorRescate" * CAR."MontoCompra") / NULLIF(CAR."ValorRescate",0)), 0)
  			ELSE 0
  		  END AS "MontoCompraInicial"
 	   , CASE WHEN Portafolio."IdMatrizAgrupadorAPV" = 0 THEN 'N' ELSE 'S' END AS "PortafolioAPV"
    FROM "BSCL.Custodia.BD::CUS.Cartera" 					AS CAR
 	
    LEFT JOIN resultadoTotal 								AS resTot 			ON resTot."FolioCarteraOrigen" = CAR."FolioCartera"       
    LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" 			AS CLI 				ON (CLI."IdEmpresa" = :p_IdEmpresa AND CLI."IdCliente" = CAR."IdCliente")  
    LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio"			AS Portafolio	 	ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
    LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" 	AS MatrizAgrupador 	ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
    LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI 		ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 		AS inst3 			ON inst3."IdInstrumento" = CAR."IdInstrumento"
    LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" 	AS REG 				ON REG."IdRegimenFondoMutuo" = CAR."IdRegimenFondoMutuo"
    LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV"AS MAT_NOAPV 		ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
    LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" 			AS MADOR			ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
	LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPortafolio" AS CIP ON CIP."IdPortafolio" = Portafolio."IdPortafolio" AND CIP."CodigoTipoIndicador" = 'COBROCUST'
	
    WHERE 

CAR."IdEmpresa" = :p_IdEmpresa
AND CAR."IdEstado" = 'VIG'
AND CAR."IdTipoInstrumento" NOT IN ('PRT', 'PRF', 'EMP', 'ERF', 
	'SIM', 'PAC', 'REP', 'RRV', 'RRF', 'FWD', 'FDO', 'FIP', 'FBC', 'FIC', 'FUF', 'IOA', 'IOE', 'IOI', 'IOD', 'IOB', 'IOC', 'IOU', 'IOM', 'IOF', 'FBO', 'DER')

<<CASE.CAR.p_IdCliente>>
<<CASE.CAR.p_IdPortafolio>>
<<CASE.CAR.p_IdClasificacionTipoInstrumento>>
<<CASE.CAR.p_IdTipoInstrumento>> 
<<CASE.CAR.p_IdInstrumento>>
<<CASE.CAR.p_Nemotecnico>>
    
<<FECHACONSULTA.CAR.p_TipoFechaConsulta>>
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
   , "CobroComisionesCustodia"
   , "OpcionCobro"
   , "MontoCompraInicial"
   , "PortafolioAPV"
FROM custodia
    
