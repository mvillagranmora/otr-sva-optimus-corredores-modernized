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
   ,"IdMatrizAgrupador"
   ,"EsADC"
   ,"IdPersona"
   ,"IdRegimenTributario"
   ,"RegimenTributario"
   ,"MontoCompraCarteraOrigen"
   ,"ValorFactorMonedaInstrumento"
   ,"RazonSocialInstrumento"
   ,"EsPagaderoEn"
   ,"CodigoRegimenTipoAhorro"
   , "CobroComisionesCustodia"
   , "OpcionCobro"
   ,"MontoCompraInicial"
   , "PortafolioAPV"
FROM
(
SELECT
		GAR."IdClasificacionTipoInstrumento"
	   ,GAR."IdTipoInstrumento"
	   ,GAR."IdInstrumento"
	   ,case when GAR."IdClasificacionTipoInstrumento" = 'IF' then GAR."NemoSVS" else GAR."Nemotecnico" end as "Nemotecnico"
	   ,CASE WHEN IFNULL(GAR."IdEmisor",'') <> '' THEN IFNULL(GAR."IdEmisor",'') ELSE inst4."IdEmisor" END AS "IdEmisor"
	   ,case when GAR."IdClasificacionTipoInstrumento" = 'IF' then GAR."EsFungible" else 'N' end as "EsFungible"
	   ,GAR."IdCliente"
	   ,GAR."IdPortafolio"
	   ,COALESCE(MADOR."NombreProductoAgrupador", 'Otros (No APV)') AS "Producto"
	   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN :v_IdBovedaDefecto ELSE CAR."IdBoveda" END AS "IdBoveda"
	   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN  0 ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio",0) END AS "IdCuentaCustodioPortafolio"
	   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN '' ELSE COALESCE(CCPI."Codigo",'') END AS "NumeroCuentaBoveda"  
	   ,cast(IFNULL(case when GAR."IdClasificacionTipoInstrumento" = 'IF'  then GAR."INGValorRescate" else GAR."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
	   ,COALESCE(GAR."PrecioCompraOperacionOrigen",0) as "PrecioCompra"						
	   ,COALESCE(GAR."INGMontoATasaGarantiaActual",0) as "MontoCompra"				
--	   ,COALESCE(GAR."INGMontoGarantiaActualMonedaInstrumento",0) as "ValorCompra" 
	   ,COALESCE(GAR."PrecioCompraOperacionOrigen" * GAR."INGCantidad",0) as "ValorCompra"
	   ,COALESCE(GAR."INGPrecioMercadoActual",0) as "INGPrecioMercadoActual" 		
	   ,COALESCE(GAR."INGTasaMercadoActual",0) as "INGTasaMercadoActual" 			
	   ,COALESCE(GAR."INGMontoMercadoActualMonedaInstrumento",0) as "ValorMercado"
	   ,COALESCE(GAR."INGMontoATasaMercadoActual",0) as "MontoMercado"
	   ,CASE
	   		WHEN IFNULL(GAR."IdTipoReajuste",'')<>'' THEN IFNULL(GAR."IdTipoReajuste",'')
	   		WHEN IFNULL(inst4."IdTipoReajuste",'')<>'' THEN IFNULL(inst4."IdTipoReajuste",'')
			WHEN IFNULL(GAR."IdMonedaInstrumento",'')<>'' THEN IFNULL(GAR."IdMonedaInstrumento",'')			   		
	   		ELSE IFNULL(inst4."IdMoneda",'')
		END as "IdMonedaInstrReajuste"
		
	   ,CASE 
	   		WHEN IFNULL(GAR."IdMonedaSistema",'')<>'' THEN IFNULL(GAR."IdMonedaSistema",'')
	   		WHEN IFNULL(GAR."IdTipoReajuste",'')<>'' THEN IFNULL(GAR."IdTipoReajuste",'')
	   		WHEN IFNULL(GAR."IdMonedaInstrumento",'')<>'' THEN IFNULL(GAR."IdMonedaInstrumento",'')
	   		WHEN IFNULL(inst4."IdTipoReajuste",'')<>'' THEN IFNULL(inst4."IdTipoReajuste",'')
	   		ELSE IFNULL(inst4."IdMoneda",'')
		END AS "IdMonedaPrincipal"
	    ,CASE 
	   		WHEN IFNULL(GAR."IdMonedaEmision",'')<>'' THEN IFNULL(GAR."IdMonedaEmision",'')
	   		WHEN IFNULL(GAR."IdTipoReajuste",'')<>'' THEN IFNULL(GAR."IdTipoReajuste",'')
	   		WHEN IFNULL(GAR."IdMonedaInstrumento",'')<>'' THEN IFNULL(GAR."IdMonedaInstrumento",'')
	   		WHEN IFNULL(inst4."IdTipoReajuste",'')<>'' THEN IFNULL(inst4."IdTipoReajuste",'')
	   		ELSE IFNULL(inst4."IdMoneda",'')
		END AS "IdMonedaEmision"		
	   ,GAR."INGMontoInteresCorridoGarantiaActual" AS "InteresCorrido"
	   ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
       ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
       ,CLI."IdPersona"
       ,COALESCE(REG."IdRegimenFondoMutuo",'') AS "IdRegimenTributario"
       ,COALESCE(REG."Descripcion",'') AS "RegimenTributario"
       ,COALESCE(CAR."EGRMonto",0.0) AS "MontoCompraCarteraOrigen"
       ,CAR."ValorFactorMonedaInstrumento"
       ,inst4."RazonSocial" AS "RazonSocialInstrumento"
	   ,CASE 
        	WHEN IFNULL(CAR."IdMonedaInstrumento",'') = 'UF'
        		THEN 'CLP'
        	ELSE 
        		CAR."IdMonedaInstrumento"
        END          AS "EsPagaderoEn"
        , COALESCE(REGTRIB."CodigoRegimenTipoAhorro",'') AS "CodigoRegimenTipoAhorro"
        , inst4."CobroComisionesCustodia" AS "CobroComisionesCustodia"
 	   , COALESCE(CIP."ValorIndicador",'0') AS "OpcionCobro"
 	   , CASE 
  			WHEN CAR."IdClasificacionTipoInstrumento" = 'RF' THEN
  				COALESCE(((GAR."INGCantidad" * CAR."MontoCompra") / NULLIF(CAR."Cantidad",0)),0)
  			WHEN CAR."IdClasificacionTipoInstrumento" = 'RV' THEN
  				(GAR."INGCantidad" * CAR."PrecioCompra")
  			WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN
  				COALESCE(((GAR."INGValorRescate" * CAR."MontoCompra") / NULLIF(CAR."ValorRescate",0)), 0)
  			ELSE 0
  		  END AS "MontoCompraInicial"
 	   , CASE WHEN Portafolio."IdMatrizAgrupadorAPV" = 0 THEN 'N' ELSE 'S' END AS "PortafolioAPV"
	FROM "BSCL.Custodia.BD::HIS.Garantia" AS GAR
	INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as CAR ON CAR."IdCartera" = GAR."IdCarteraOrigen" AND CAR."FechaRespaldo" = :v_FechaConsulta
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =:p_IdEmpresa AND CLI."IdCliente" = GAR."IdCliente")
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = GAR."IdPortafolio" AND Portafolio."IdCliente" = GAR."IdCliente"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
	LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = GAR."IdInstrumento"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = CAR."IdRegimenFondoMutuo"
	LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
	LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPortafolio" AS CIP ON CIP."IdPortafolio" = Portafolio."IdPortafolio" AND CIP."CodigoTipoIndicador" = 'COBROCUST'
	WHERE
		GAR."IdEmpresa" =:p_IdEmpresa
	AND GAR."FechaRespaldo" = :v_FechaConsulta
	AND GAR."IdEstado" = 'VIG'
	
	<<CASE.GAR.p_IdCliente>>
	<<CASE.GAR.p_IdPortafolio>>
	<<CASE.GAR.p_IdClasificacionTipoInstrumento>>
	<<CASE.GAR.p_IdTipoInstrumento>> 
	<<CASE.GAR.p_IdInstrumento>>
	<<CASE.GAR.p_Nemotecnico>>
	<<FECHACONSULTA.GAR.p_TipoFechaConsulta>>
	<<COALESCE.MatrizAgrupador.p_IdProductoAgrupador>>
)
