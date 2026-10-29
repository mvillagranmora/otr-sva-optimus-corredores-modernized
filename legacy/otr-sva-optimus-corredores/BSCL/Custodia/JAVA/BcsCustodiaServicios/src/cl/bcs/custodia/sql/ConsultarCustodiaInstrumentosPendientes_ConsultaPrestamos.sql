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
   ,"RazonSocialInstrumento"
FROM
(
SELECT
		sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
	   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
	   ,sby."IdInstrumento" as "IdInstrumento"
	   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
	   ,sby."IdEmisor" as "IdEmisor"
	   ,case when sby."IdClasificacionTipoInstrumento"= 'IF' then sby."EsFungible" else 'N' end as "EsFungible"
	   ,PRE."IdCliente"
	   ,PRE."IdPortafolio"
	   ,COALESCE(MADOR."NombreProductoAgrupador", 'Otros (No APV)') AS "Producto"
	   ,CASE 
	   		WHEN :p_AgrupaPorBoveda='N' THEN :v_IdBovedaDefecto 
	   		WHEN :p_AgrupaPorBoveda='S' AND IFNULL(sby."IdBoveda",'')<>'' THEN sby."IdBoveda" 
	   		ELSE 'DCV' 
	   	END AS "IdBoveda"
	   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN  0 ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio",0) END AS "IdCuentaCustodioPortafolio"
	   ,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF'  then PRE."INGValorRescate" else PRE."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
	   ,COALESCE(sby."INGPrecioSubyacente",0) as "PrecioCompra"						
	   ,COALESCE(sby."INGMontoATasaSubyacenteActual",0) as "MontoCompra"			
	   ,COALESCE(sby."INGMontoSubyacenteActualMonedaInstrumento",0) as "ValorCompra" 	
	   ,COALESCE(sby."INGPrecioMercadoActual",0) as "INGPrecioMercadoActual"			
	   ,COALESCE(sby."INGTasaMercadoActual",0) as "INGTasaMercadoActual"				
	   ,COALESCE(sby."INGMontoMercadoActualMonedaInstrumento",0) as "ValorMercado"
	   ,COALESCE(sby."INGMontoATasaMercadoActual",0) as "MontoMercado"
	   ,CASE
	   		WHEN ifnull(sby."IdMonedaInstrumento",'') <> '' THEN sby."IdMonedaInstrumento"
	   		WHEN ifnull(sby."IdTipoReajusteSubyacente",'') <> '' THEN sby."IdTipoReajusteSubyacente"
	   		ELSE ifnull(sby."IdMonedaSistema",'')
		END AS "IdMonedaInstrReajuste"
	   ,sby."IdMonedaSistema" AS "IdMonedaPrincipal"
	   ,CASE
	   		WHEN IFNULL(sby."IdMonedaEmision",'') <> '' THEN sby."IdMonedaEmision"
	   		WHEN IFNULL(sby."IdTipoReajusteSubyacente",'') <> '' THEN sby."IdTipoReajusteSubyacente"
	   		ELSE sby."IdMonedaInstrumento"
		END AS "IdMonedaEmision"
	   ,sby."INGMontoInteresCorridoSubyacenteActual" as "InteresCorrido"
	   ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
       ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
       ,CLI."IdPersona"
       ,inst3."RazonSocial" AS "RazonSocialInstrumento"
	FROM "BSCL.Custodia.BD::CUS.Cartera" AS PRE
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (PRE."FolioCartera" = ultSub."FolioCarteraRelacionada")
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on (sby."IdSubyacente" = ultSub."IdSubyacente")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =:p_IdEmpresa AND CLI."IdCliente" = PRE."IdCliente")
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PRE."IdPortafolio" AND Portafolio."IdCliente" = PRE."IdCliente"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
	LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PRE."IdCuentaCustodioPortafolio"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
	LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = sby."IdInstrumento"
	WHERE
		PRE."IdEmpresa" =:p_IdEmpresa
	AND PRE."IdEstado" = 'VIG'
	AND	PRE."IdCliente" = case when:p_IdCliente > 0 then:p_IdCliente else PRE."IdCliente" END
	AND	PRE."IdPortafolio" IN (:p_IdPortafolio)
	AND
	(
	       (:p_TipoFechaConsulta = 'LIQ' AND PRE."INGFechaLiquidacion" <= :v_FechaConsulta)
	    OR (:p_TipoFechaConsulta = 'ADQ' AND PRE."INGFechaOperacion" <= :v_FechaConsulta)
	)
	AND	sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(:p_IdClasificacionTipoInstrumento,'') <> '' THEN :p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
	AND	sby."IdTipoInstrumento" = CASE WHEN IFNULL(:p_IdTipoInstrumento,'') <> '' THEN :p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
	AND	sby."IdInstrumento" IN (:p_IdInstrumento)
	AND sby."Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE sby."Nemotecnico" END
	AND PRE."IdTipoInstrumento" = 'PRT'
	AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
	)
