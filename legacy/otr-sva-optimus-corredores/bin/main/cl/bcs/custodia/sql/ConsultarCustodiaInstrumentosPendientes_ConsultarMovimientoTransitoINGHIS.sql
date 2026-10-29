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
FROM
(
	SELECT
		COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
	   ,COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
	   ,COALESCE(rel."IdInstrumento", HMOV."IdInstrumento") AS "IdInstrumento"
	   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",HMOV."NemoSVS") else COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") end as "Nemotecnico"
	   ,CASE WHEN COALESCE(rel."IdEmisor",HMOV."IdEmisor",'') <> '' THEN IFNULL(COALESCE(rel."IdEmisor",HMOV."IdEmisor"),'') ELSE inst4."IdEmisor" END AS "IdEmisor"
	   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."EsFungible",HMOV."EsFungible") else 'N' end as "EsFungible"
	   ,COALESCE(rel."IdCliente",HMOV."IdCliente") AS "IdCliente"
	   ,COALESCE(rel."IdPortafolio",HMOV."IdPortafolio") AS "IdPortafolio"
	   ,COALESCE(MADOR."NombreProductoAgrupador", 'Otros (No APV)') AS "Producto"
	   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN :v_IdBovedaDefecto 
	   		 WHEN :p_AgrupaPorBoveda='S' AND IFNULL(HMOV."IdBoveda",'')<>'' THEN IFNULL(HMOV."IdBoveda",'')
	   		 ELSE :v_IdBovedaDefecto 
	   	END AS "IdBoveda"
	   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN  0 ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio",0) END AS "IdCuentaCustodioPortafolio"
	   ,CASE
	   		WHEN COALESCE(rel."IdTipoReajuste",HMOV."IdTipoReajuste",'') <> '' THEN COALESCE(rel."IdTipoReajuste",HMOV."IdTipoReajuste",'')
	   		WHEN COALESCE(rel."IdMonedaInstrumento",HMOV."IdMonedaInstrumento",'') <> '' THEN COALESCE(rel."IdMonedaInstrumento",HMOV."IdMonedaInstrumento",'')
	   		WHEN COALESCE(rel."IdMonedaSistema",HMOV."IdMonedaSistema",'') <> '' THEN COALESCE(rel."IdMonedaSistema",HMOV."IdMonedaSistema",'')
	   		WHEN ifnull(inst4."IdTipoReajuste",'')<> '' THEN ifnull(inst4."IdTipoReajuste",'')
	   		ELSE inst4."IdMoneda"
	   	END AS "IdMonedaInstrReajuste"
	   ,CAST(COALESCE(case when HMOV."IdClasificacionTipoInstrumento" = 'IF' then HMOV."ValorRescate" else HMOV."Cantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
	   ,CASE
	   		WHEN COALESCE(rel."IdMonedaSistema",HMOV."IdMonedaSistema",'') <> '' THEN COALESCE(rel."IdMonedaSistema",HMOV."IdMonedaSistema",'')
	   		WHEN COALESCE(rel."IdTipoReajuste",HMOV."IdTipoReajuste",'') <> '' THEN COALESCE(rel."IdTipoReajuste",HMOV."IdTipoReajuste",'')
	   		WHEN COALESCE(rel."IdMonedaInstrumento",HMOV."IdMonedaInstrumento",'') <> '' THEN COALESCE(rel."IdMonedaInstrumento",HMOV."IdMonedaInstrumento",'')
	   		WHEN COALESCE(rel."IdMonedaSistema",HMOV."IdMonedaSistema",'') <> '' THEN COALESCE(rel."IdMonedaSistema",HMOV."IdMonedaSistema",'')
	   		WHEN ifnull(inst4."IdTipoReajuste",'')<> '' THEN ifnull(inst4."IdTipoReajuste",'')
	   		ELSE inst4."IdMoneda"
	   	END AS "IdMonedaPrincipal"
	   ,CASE
	   		WHEN COALESCE(rel."IdMonedaEmision",HMOV."IdMonedaEmision",'') <> '' THEN COALESCE(rel."IdMonedaEmision",HMOV."IdMonedaEmision",'')
	   		WHEN COALESCE(rel."IdTipoReajuste",HMOV."IdTipoReajuste",'') <> '' THEN COALESCE(rel."IdTipoReajuste",HMOV."IdTipoReajuste",'')
	   		WHEN COALESCE(rel."IdMonedaInstrumento",HMOV."IdMonedaInstrumento",'') <> '' THEN COALESCE(rel."IdMonedaInstrumento",HMOV."IdMonedaInstrumento",'')
	   		WHEN COALESCE(rel."IdMonedaSistema",HMOV."IdMonedaSistema",'') <> '' THEN COALESCE(rel."IdMonedaSistema",HMOV."IdMonedaSistema",'')
	   		WHEN COALESCE(inst4."IdTipoReajuste",'') <> '' THEN ifnull(inst4."IdTipoReajuste",'')
	   		ELSE inst4."IdMoneda"
	   	END AS "IdMonedaEmision"
	  ,COALESCE(rel."EsNominal",HMOV."EsNominal") AS "EsNominal"
	  ,COALESCE(rel."IdTipoOperacion",HMOV."IdTipoOperacion") AS "IdTipoOperacion"
	  ,TipoOperacion."AccionCustodia"
	  ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
      ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
      ,CLI."IdPersona"
      ,COALESCE (REG."IdRegimenFondoMutuo",'') AS "IdRegimenTributario"
      ,COALESCE (REG."Descripcion",'') AS "RegimenTributario"
      ,inst4."RazonSocial" AS "RazonSocialInstrumento"
	FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS HMOV
		LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = HMOV."IdMovimientoCarteraEnTransito"  
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON HMOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =:p_IdEmpresa AND CLI."IdCliente" = HMOV."IdCliente")
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = HMOV."IdPortafolio" AND Portafolio."IdCliente" = HMOV."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = HMOV."IdCuentaCustodioPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",HMOV."IdInstrumento")
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = HMOV."IdRegimenFondoMutuo"
		LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
	WHERE
			HMOV."IdEmpresa" = :p_IdEmpresa
		AND TO_NVARCHAR(HMOV."FechaRespaldo",'YYYY-MM-DD') = :v_FechaConsulta
		AND	HMOV."IdEstado" = 'PEN'
		AND	HMOV."IdCliente" = CASE WHEN :p_IdCliente > 0 THEN :p_IdCliente else HMOV."IdCliente" END
		AND	HMOV."IdPortafolio" IN (:p_IdPortafolio)
		AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
		AND
			(
				TipoOperacion."AccionCustodia" = 'INGRESO' AND  HMOV."EsNominal" = 'N'
			)
		AND	COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(:p_IdClasificacionTipoInstrumento,'') <> '' THEN :p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") END
		AND	COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") = CASE WHEN COALESCE(:p_IdTipoInstrumento,'') <> '' THEN :p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") END
		AND	COALESCE(rel."IdInstrumento",HMOV."IdInstrumento") IN (:p_IdInstrumento)
		AND	COALESCE(rel."Nemotecnico", HMOV."Nemotecnico") = CASE WHEN COALESCE(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE COALESCE(rel."Nemotecnico", HMOV."Nemotecnico") END
		AND
		(
			(:v_FechaConsulta >= HMOV."FechaOperacion" AND :v_FechaConsulta <= HMOV."FechaCertificacionRegistro" AND HMOV."FechaCertificacionRegistro" >= '0001-01-02')
			OR
			(:v_FechaConsulta >= HMOV."FechaOperacion" AND HMOV."FechaCertificacionRegistro" = '0001-01-01' )
		)
		AND HMOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
		AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
		AND HMOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV', 'PRESTA','EMPRES')
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
FROM
(
	SELECT
		COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
	   ,COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
	   ,COALESCE(rel."IdInstrumento", HMOV."IdInstrumento") AS "IdInstrumento"
	   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",HMOV."NemoSVS") else COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") end as "Nemotecnico"
	   ,CASE WHEN COALESCE(rel."IdEmisor",HMOV."IdEmisor",'') <> '' THEN IFNULL(COALESCE(rel."IdEmisor",HMOV."IdEmisor"),'') ELSE inst4."IdEmisor" END AS "IdEmisor"
	   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."EsFungible",HMOV."EsFungible") else 'N' end as "EsFungible"
	   ,COALESCE(rel."IdCliente",HMOV."IdCliente") AS "IdCliente"
	   ,COALESCE(rel."IdPortafolio",HMOV."IdPortafolio") AS "IdPortafolio"
	   ,COALESCE(MADOR."NombreProductoAgrupador", 'Otros (No APV)') AS "Producto"
	   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN :v_IdBovedaDefecto 
	   		 WHEN :p_AgrupaPorBoveda='S' AND IFNULL(HMOV."IdBoveda",'')<>'' THEN IFNULL(HMOV."IdBoveda",'')
	   		 ELSE :v_IdBovedaDefecto 
	   	END AS "IdBoveda"
	   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN  0 ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio",0) END AS "IdCuentaCustodioPortafolio"
	   ,CASE
	   		WHEN COALESCE(rel."IdTipoReajuste",HMOV."IdTipoReajuste",'') <> '' THEN COALESCE(rel."IdTipoReajuste",HMOV."IdTipoReajuste",'')
	   		WHEN COALESCE(rel."IdMonedaInstrumento",HMOV."IdMonedaInstrumento",'') <> '' THEN COALESCE(rel."IdMonedaInstrumento",HMOV."IdMonedaInstrumento",'')
	   		WHEN COALESCE(rel."IdMonedaSistema",HMOV."IdMonedaSistema",'') <> '' THEN COALESCE(rel."IdMonedaSistema",HMOV."IdMonedaSistema",'')
	   		WHEN ifnull(inst4."IdTipoReajuste",'')<> '' THEN ifnull(inst4."IdTipoReajuste",'')
	   		ELSE inst4."IdMoneda"
	   	END AS "IdMonedaInstrReajuste"
	   ,CAST(COALESCE(case when HMOV."IdClasificacionTipoInstrumento" = 'IF' then HMOV."ValorRescate" else HMOV."Cantidad" end,0) * -1 as DECIMAL(26,8)) as "Cantidad"
	   ,CASE
	   		WHEN COALESCE(rel."IdMonedaSistema",HMOV."IdMonedaSistema",'') <> '' THEN COALESCE(rel."IdMonedaSistema",HMOV."IdMonedaSistema",'')
	   		WHEN COALESCE(rel."IdTipoReajuste",HMOV."IdTipoReajuste",'') <> '' THEN COALESCE(rel."IdTipoReajuste",HMOV."IdTipoReajuste",'')
	   		WHEN COALESCE(rel."IdMonedaInstrumento",HMOV."IdMonedaInstrumento",'') <> '' THEN COALESCE(rel."IdMonedaInstrumento",HMOV."IdMonedaInstrumento",'')
	   		WHEN COALESCE(rel."IdMonedaSistema",HMOV."IdMonedaSistema",'') <> '' THEN COALESCE(rel."IdMonedaSistema",HMOV."IdMonedaSistema",'')
	   		WHEN ifnull(inst4."IdTipoReajuste",'')<> '' THEN ifnull(inst4."IdTipoReajuste",'')
	   		ELSE inst4."IdMoneda"
	   	END AS "IdMonedaPrincipal"
	   ,CASE
	   		WHEN COALESCE(rel."IdMonedaEmision",HMOV."IdMonedaEmision",'') <> '' THEN COALESCE(rel."IdMonedaEmision",HMOV."IdMonedaEmision",'')
	   		WHEN COALESCE(rel."IdTipoReajuste",HMOV."IdTipoReajuste",'') <> '' THEN COALESCE(rel."IdTipoReajuste",HMOV."IdTipoReajuste",'')
	   		WHEN COALESCE(rel."IdMonedaInstrumento",HMOV."IdMonedaInstrumento",'') <> '' THEN COALESCE(rel."IdMonedaInstrumento",HMOV."IdMonedaInstrumento",'')
	   		WHEN COALESCE(rel."IdMonedaSistema",HMOV."IdMonedaSistema",'') <> '' THEN COALESCE(rel."IdMonedaSistema",HMOV."IdMonedaSistema",'')
	   		WHEN COALESCE(inst4."IdTipoReajuste",'') <> '' THEN ifnull(inst4."IdTipoReajuste",'')
	   		ELSE inst4."IdMoneda"
	   	END AS "IdMonedaEmision"
	  ,COALESCE(rel."EsNominal",HMOV."EsNominal") AS "EsNominal"
      ,COALESCE(rel."IdTipoOperacion",HMOV."IdTipoOperacion") AS "IdTipoOperacion" 
      ,COALESCE(rel."EsVentaDescubierta",MOV."EsVentaDescubierta") AS "EsVentaDescubierta"
      ,TipoOperacion."AccionCustodia"
      ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
      ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
      ,CLI."IdPersona"
      ,COALESCE (REG."IdRegimenFondoMutuo",'') AS "IdRegimenTributario"
      ,COALESCE (REG."Descripcion",'') AS "RegimenTributario"
      ,inst4."RazonSocial" AS "RazonSocialInstrumento"
	FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS HMOV
	INNER JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV ON HMOV."IdMovimientoCarteraEnTransito" = MOV."IdMovimientoCarteraEnTransito"
		LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = HMOV."IdMovimientoCarteraEnTransito"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON HMOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =:p_IdEmpresa AND CLI."IdCliente" = HMOV."IdCliente")
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = HMOV."IdPortafolio" AND Portafolio."IdCliente" = HMOV."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = HMOV."IdCuentaCustodioPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",HMOV."IdInstrumento")
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = HMOV."IdRegimenFondoMutuo"
		LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
	WHERE
			HMOV."IdEmpresa" = :p_IdEmpresa
		AND TO_NVARCHAR(HMOV."FechaRespaldo",'YYYY-MM-DD') = :v_FechaConsulta
		AND	HMOV."IdEstado" = 'PEN'
		AND	HMOV."IdCliente" = CASE WHEN :p_IdCliente > 0 THEN :p_IdCliente else HMOV."IdCliente" END
		AND	HMOV."IdPortafolio" IN (:p_IdPortafolio)
		AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
		AND
			(
				TipoOperacion."AccionCustodia" = 'RETIRO' AND  HMOV."EsNominal" = 'N'
			)
		AND	COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(:p_IdClasificacionTipoInstrumento,'') <> '' THEN :p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") END
		AND	COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") = CASE WHEN COALESCE(:p_IdTipoInstrumento,'') <> '' THEN :p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") END
		AND	COALESCE(rel."IdInstrumento",HMOV."IdInstrumento") IN (:p_IdInstrumento)
		AND	COALESCE(rel."Nemotecnico", HMOV."Nemotecnico") = CASE WHEN COALESCE(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE COALESCE(rel."Nemotecnico", HMOV."Nemotecnico") END
		AND
		(
			(:v_FechaConsulta >= HMOV."FechaOperacion" AND :v_FechaConsulta <= HMOV."FechaCertificacionRegistro" AND HMOV."FechaCertificacionRegistro" >= '0001-01-02')
			OR
			(:v_FechaConsulta >= HMOV."FechaOperacion" AND HMOV."FechaCertificacionRegistro" = '0001-01-01' )
		)
		AND HMOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
		AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
		AND HMOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV', 'PRESTA','EMPRES')
)
