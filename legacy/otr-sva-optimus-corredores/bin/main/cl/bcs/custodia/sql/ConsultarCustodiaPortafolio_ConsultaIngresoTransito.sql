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
   ,"IdMonedaInstrReajuste"
   ,"IdMonedaEmision"
   ,"IdMonedaPrincipal"
   ,"EsNominal"
   ,"IdTipoOperacion"
   ,'N' AS "EsVentaDescubierta"   
   ,'N' AS "AfectaPresunto"
   ,"AccionCustodia"
   ,"IdMatrizAgrupador"
   ,"EsADC"
   ,"IdPersona"
   ,"IdRegimenTributario"
   ,"RegimenTributario"
   ,"RazonSocialInstrumento"
   ,"IdTipoProducto"
   , "CodigoRegimenTipoAhorro"
   ,"EsPagaderoEn"
FROM
(
	SELECT
		COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
	   ,COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
	   ,COALESCE(rel."IdInstrumento", MOV."IdInstrumento") AS "IdInstrumento"
	   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",MOV."NemoSVS") else COALESCE(rel."Nemotecnico",MOV."Nemotecnico") end as "Nemotecnico"
	   ,CASE WHEN COALESCE(rel."IdEmisor",MOV."IdEmisor",'') <> '' THEN IFNULL(COALESCE(rel."IdEmisor",MOV."IdEmisor"),'') ELSE inst4."IdEmisor" END AS "IdEmisor"
	   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."EsFungible",MOV."EsFungible") else 'N' end as "EsFungible"
	   ,COALESCE(rel."IdCliente",MOV."IdCliente") AS "IdCliente"
	   ,COALESCE(rel."IdPortafolio",MOV."IdPortafolio") AS "IdPortafolio"
	   ,COALESCE(MADOR."NombreProductoAgrupador", 'Otros (No APV)') AS "Producto"
	   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN :v_IdBovedaDefecto 
	   		 WHEN :p_AgrupaPorBoveda='S' AND IFNULL(MOV."IdBoveda",'')<>'' THEN IFNULL(MOV."IdBoveda",'')
	   		 ELSE :v_IdBovedaDefecto 
	   	END AS "IdBoveda"
	   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN  0 ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio",0) END AS "IdCuentaCustodioPortafolio"
	   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN '' ELSE COALESCE(CCPI."Codigo",'') END AS "NumeroCuentaBoveda"
	   ,CASE
	   		WHEN COALESCE(rel."IdTipoReajuste",MOV."IdTipoReajuste",'') <> '' THEN COALESCE(rel."IdTipoReajuste",MOV."IdTipoReajuste",'')
	   		WHEN COALESCE(rel."IdMonedaInstrumento",MOV."IdMonedaInstrumento",'') <> '' THEN COALESCE(rel."IdMonedaInstrumento",MOV."IdMonedaInstrumento",'')
	   		WHEN COALESCE(rel."IdMonedaSistema",MOV."IdMonedaSistema",'') <> '' THEN COALESCE(rel."IdMonedaSistema",MOV."IdMonedaSistema",'')
	   		WHEN ifnull(inst4."IdTipoReajuste",'')<> '' THEN ifnull(inst4."IdTipoReajuste",'')
	   		ELSE inst4."IdMoneda"
	   	END AS "IdMonedaInstrReajuste"
	   ,CAST(COALESCE(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
	   ,CASE
	   		WHEN COALESCE(rel."IdMonedaSistema",MOV."IdMonedaSistema",'') <> '' THEN COALESCE(rel."IdMonedaSistema",MOV."IdMonedaSistema",'')
	   		WHEN COALESCE(rel."IdTipoReajuste",MOV."IdTipoReajuste",'') <> '' THEN COALESCE(rel."IdTipoReajuste",MOV."IdTipoReajuste",'')
	   		WHEN COALESCE(rel."IdMonedaInstrumento",MOV."IdMonedaInstrumento",'') <> '' THEN COALESCE(rel."IdMonedaInstrumento",MOV."IdMonedaInstrumento",'')
	   		WHEN COALESCE(rel."IdMonedaSistema",MOV."IdMonedaSistema",'') <> '' THEN COALESCE(rel."IdMonedaSistema",MOV."IdMonedaSistema",'')
	   		WHEN ifnull(inst4."IdTipoReajuste",'')<> '' THEN ifnull(inst4."IdTipoReajuste",'')
	   		ELSE inst4."IdMoneda"
	   	END AS "IdMonedaPrincipal"
	   ,CASE
	   		WHEN COALESCE(rel."IdMonedaEmision",MOV."IdMonedaEmision",'') <> '' THEN COALESCE(rel."IdMonedaEmision",MOV."IdMonedaEmision",'')
	   		WHEN COALESCE(rel."IdTipoReajuste",MOV."IdTipoReajuste",'') <> '' THEN COALESCE(rel."IdTipoReajuste",MOV."IdTipoReajuste",'')
	   		WHEN COALESCE(rel."IdMonedaInstrumento",MOV."IdMonedaInstrumento",'') <> '' THEN COALESCE(rel."IdMonedaInstrumento",MOV."IdMonedaInstrumento",'')
	   		WHEN COALESCE(rel."IdMonedaSistema",MOV."IdMonedaSistema",'') <> '' THEN COALESCE(rel."IdMonedaSistema",MOV."IdMonedaSistema",'')
	   		WHEN COALESCE(inst4."IdTipoReajuste",'') <> '' THEN ifnull(inst4."IdTipoReajuste",'')
	   		ELSE inst4."IdMoneda"
	   	END AS "IdMonedaEmision"
	  ,COALESCE(rel."EsNominal",MOV."EsNominal") AS "EsNominal"
	  ,COALESCE(rel."IdTipoOperacion",MOV."IdTipoOperacion") AS "IdTipoOperacion"
	  ,COALESCE(rel."IdTipoProducto",MOV."IdTipoProducto") AS "IdTipoProducto"
	  ,TipoOperacion."AccionCustodia"
	  ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
      ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
      ,CLI."IdPersona"
      ,COALESCE (REG."IdRegimenFondoMutuo",'') AS "IdRegimenTributario"
      ,COALESCE (REG."Descripcion",'') AS "RegimenTributario"
      ,inst4."RazonSocial" AS "RazonSocialInstrumento"
      , COALESCE(REGTRIB."CodigoRegimenTipoAhorro",'') AS "CodigoRegimenTipoAhorro"
      ,CASE WHEN IFNULL(rel."IdMonedaInstrumento",'') = 'UF' THEN 'CLP' ELSE rel."IdMonedaInstrumento" END AS "EsPagaderoEn"   -- Moneda de Pago
	FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
		LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = MOV."IdMovimientoCarteraEnTransito"  
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =:p_IdEmpresa AND CLI."IdCliente" = MOV."IdCliente")
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = MOV."IdCuentaCustodioPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",MOV."IdInstrumento")
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = MOV."IdRegimenFondoMutuo"
		LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
		LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
	WHERE
			MOV."IdEmpresa" = :p_IdEmpresa
		AND	MOV."IdCliente" = CASE WHEN :p_IdCliente > 0 THEN :p_IdCliente else MOV."IdCliente" END
		AND	MOV."IdPortafolio" = CASE WHEN :p_IdPortafolio > 0 THEN :p_IdPortafolio else MOV."IdPortafolio" END
		AND	MOV."IdEstado" = 'PEN'
		AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
		AND
			(
				TipoOperacion."AccionCustodia" = 'INGRESO' AND  MOV."EsNominal" = 'N'
--				OR
--				(TipoOperacion."AccionCustodia" = 'RETIRO' AND MOV."EsNominal" = 'S' AND MOV."IdTipoOperacionOrigen" IN ('VENSIM','COMSIM'))
			)
		AND	COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(:p_IdClasificacionTipoInstrumento,'') <> '' THEN :p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") END
		AND	COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") = CASE WHEN COALESCE(:p_IdTipoInstrumento,'') <> '' THEN :p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") END
		AND	COALESCE(rel."IdInstrumento",MOV."IdInstrumento") = CASE WHEN COALESCE(:p_IdInstrumento,0) > 0 THEN :p_IdInstrumento ELSE COALESCE(rel."IdInstrumento",MOV."IdInstrumento") END
		AND	COALESCE(rel."Nemotecnico", MOV."Nemotecnico") = CASE WHEN COALESCE(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE COALESCE(rel."Nemotecnico", MOV."Nemotecnico") END
		AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
		<<COALESCE.MatrizAgrupador.p_IdProductoAgrupador>>
		AND MOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV', 'PRESTA','EMPRES')
)
--EGRESOS TRANSITO
UNION ALL
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
   ,"IdMonedaInstrReajuste"
   ,"IdMonedaEmision"
   ,"IdMonedaPrincipal"
   ,"EsNominal"
   ,"IdTipoOperacion"
   ,"EsVentaDescubierta"   
   ,(CASE
   	   WHEN ("EsVentaDescubierta" = 'S') THEN 'S'
   	   ELSE 'N'
    END) AS "AfectaPresunto"
    ,"AccionCustodia"
    ,"IdMatrizAgrupador"
    ,"EsADC"
    ,"IdPersona"
    ,"IdRegimenTributario"
    ,"RegimenTributario"
    ,"RazonSocialInstrumento"
    ,"IdTipoProducto"
    ,"CodigoRegimenTipoAhorro"
    ,"EsPagaderoEn"
FROM
(
	SELECT
		MOV."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
	   ,MOV."IdTipoInstrumento" AS "IdTipoInstrumento" 
	   ,MOV."IdInstrumento" AS "IdInstrumento"
	   ,CASE WHEN MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."NemoSVS" else MOV."Nemotecnico" end as "Nemotecnico"
	   ,CASE WHEN MOV."IdEmisor" <> '' THEN IFNULL(MOV."IdEmisor",'') ELSE inst4."IdEmisor" END AS "IdEmisor"
	   ,CASE WHEN MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."EsFungible" else 'N' end as "EsFungible"
	   ,MOV."IdCliente" AS "IdCliente"
	   ,MOV."IdPortafolio" AS "IdPortafolio"
	   ,COALESCE(MADOR."NombreProductoAgrupador", 'Otros (No APV)') AS "Producto"
	   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN :v_IdBovedaDefecto 
	   		 WHEN :p_AgrupaPorBoveda='S' AND IFNULL(MOV."IdBoveda",'')<>'' THEN IFNULL(MOV."IdBoveda",'')
	   		 ELSE :v_IdBovedaDefecto 
	   	END AS "IdBoveda"
	   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN  0 ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio",0) END AS "IdCuentaCustodioPortafolio"
	   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN '' ELSE COALESCE(CCPI."Codigo",'') END AS "NumeroCuentaBoveda"
	   ,CASE
	   		WHEN MOV."IdTipoReajuste" <> '' THEN MOV."IdTipoReajuste"
	   		WHEN MOV."IdMonedaInstrumento" <> '' THEN MOV."IdMonedaInstrumento"
	   		WHEN MOV."IdMonedaSistema" <> '' THEN MOV."IdMonedaSistema"
	   		WHEN ifnull(inst4."IdTipoReajuste",'')<> '' THEN ifnull(inst4."IdTipoReajuste",'')
	   		ELSE inst4."IdMoneda"
	   	END AS "IdMonedaInstrReajuste"
	   ,CAST(COALESCE(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) * -1 as DECIMAL(26,8)) as "Cantidad"
	   ,CASE
	   		WHEN MOV."IdMonedaSistema" <> '' THEN MOV."IdMonedaSistema"
	   		WHEN MOV."IdTipoReajuste" <> '' THEN MOV."IdTipoReajuste"
	   		WHEN MOV."IdMonedaInstrumento" <> '' THEN MOV."IdMonedaInstrumento"
	   		WHEN MOV."IdMonedaSistema" <> '' THEN MOV."IdMonedaSistema"
	   		WHEN ifnull(inst4."IdTipoReajuste",'')<> '' THEN ifnull(inst4."IdTipoReajuste",'')
	   		ELSE inst4."IdMoneda"
	   	END AS "IdMonedaPrincipal"
	   ,CASE
	   		WHEN MOV."IdMonedaEmision" <> '' THEN MOV."IdMonedaEmision"
	   		WHEN MOV."IdTipoReajuste" <> '' THEN MOV."IdTipoReajuste"
	   		WHEN MOV."IdMonedaInstrumento" <> '' THEN MOV."IdMonedaInstrumento"
	   		WHEN MOV."IdMonedaSistema" <> '' THEN MOV."IdMonedaSistema"
	   		WHEN COALESCE(inst4."IdTipoReajuste",'') <> '' THEN ifnull(inst4."IdTipoReajuste",'')
	   		ELSE inst4."IdMoneda"
	   	END AS "IdMonedaEmision"
	  ,MOV."EsNominal" AS "EsNominal"
      ,MOV."IdTipoOperacion" AS "IdTipoOperacion" 
      ,MOV."IdTipoProducto"
      ,MOV."EsVentaDescubierta" AS "EsVentaDescubierta"
      ,TipoOperacion."AccionCustodia"
      ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
      ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
      ,CLI."IdPersona"
      ,COALESCE (REG."IdRegimenFondoMutuo",'') AS "IdRegimenTributario"
      ,COALESCE (REG."Descripcion",'') AS "RegimenTributario"
      ,inst4."RazonSocial" AS "RazonSocialInstrumento"
      , COALESCE(REGTRIB."CodigoRegimenTipoAhorro",'') AS "CodigoRegimenTipoAhorro"
      ,CASE WHEN IFNULL(MOV."IdMonedaInstrumento",'') = 'UF' THEN 'CLP' ELSE MOV."IdMonedaInstrumento" END AS "EsPagaderoEn"   -- Moneda de Pago
	FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =:p_IdEmpresa AND CLI."IdCliente" = MOV."IdCliente")
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = MOV."IdCuentaCustodioPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = MOV."IdInstrumento"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = MOV."IdRegimenFondoMutuo"
		LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
		LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
	WHERE
			MOV."IdEmpresa" = :p_IdEmpresa
		AND	MOV."IdEstado" = 'PEN'
		AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
		AND
			(
				TipoOperacion."AccionCustodia" = 'RETIRO' AND  MOV."EsNominal" = 'N'
--				OR
--				(TipoOperacion."AccionCustodia" = 'INGRESO' AND MOV."EsNominal" = 'S' AND MOV."IdTipoOperacionOrigen" IN ('VENSIM','COMSIM'))
			)
			
		<<CASE.MOV.p_IdCliente>>
		<<CASE.MOV.p_IdPortafolio>>
		<<CASE.MOV.p_IdClasificacionTipoInstrumento>>
		<<CASE.MOV.p_IdTipoInstrumento>> 
		<<CASE.MOV.p_IdInstrumento>>
		<<CASE.MOV.p_Nemotecnico>>
		
		AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
		<<COALESCE.MatrizAgrupador.p_IdProductoAgrupador>>
		AND MOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV', 'PRESTA','EMPRES')
)
