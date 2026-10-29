SELECT "IdClasificacionTipoInstrumento"
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
   ,'S' AS "AfectaPresunto"
    ,"AccionCustodia"
    ,"IdMatrizAgrupador"
    ,"EsADC"
    ,"IdPersona"
    ,"IdRegimenTributario"
   ,"RegimenTributario"
   ,"RazonSocialInstrumento"
   ,"Relacion"
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
	   ,CASE
	   		WHEN COALESCE(rel."IdTipoReajuste",MOV."IdTipoReajuste",'') <> '' THEN COALESCE(rel."IdTipoReajuste",MOV."IdTipoReajuste",'')
	   		WHEN COALESCE(rel."IdMonedaInstrumento",MOV."IdMonedaInstrumento",'') <> '' THEN COALESCE(rel."IdMonedaInstrumento",MOV."IdMonedaInstrumento",'')
	   		WHEN COALESCE(rel."IdMonedaSistema",MOV."IdMonedaSistema",'') <> '' THEN COALESCE(rel."IdMonedaSistema",MOV."IdMonedaSistema",'')
	   		WHEN ifnull(inst4."IdTipoReajuste",'')<> '' THEN ifnull(inst4."IdTipoReajuste",'')
	   		ELSE inst4."IdMoneda"
	   	END AS "IdMonedaInstrReajuste"
	   ,CAST(COALESCE(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) * -1 as DECIMAL(26,8)) as "Cantidad"
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
      ,COALESCE(rel."EsVentaDescubierta",MOV."EsVentaDescubierta") AS "EsVentaDescubierta"
      ,TipoOperacion."AccionCustodia"
      ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
      ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
      ,CLI."IdPersona"
      ,COALESCE (REG."IdRegimenFondoMutuo",'') AS "IdRegimenTributario"
      ,COALESCE (REG."Descripcion",'') AS "RegimenTributario"
      ,inst4."RazonSocial" AS "RazonSocialInstrumento"
      , CASE
      		WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
            WHEN COALESCE(RELA."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
            ELSE 'TER'                                                  -- Tercero
        END AS "Relacion"
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
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" AS RELA
			    ON  RELA."IdPersonaOrigen" = :v_IdPersonaCuentaPropia
			    AND RELA."IdEmpresa" = CLI."IdEmpresa"
			    AND RELA."IdEstado" = 'ACT'
			    AND RELA."IdPersonaDestino" = CLI."IdPersona"
	WHERE
			MOV."IdEmpresa" = :p_IdEmpresa
		AND	MOV."IdCliente" = CASE WHEN :p_IdCliente > 0 THEN :p_IdCliente else MOV."IdCliente" END
		AND	MOV."IdPortafolio" IN (:p_IdPortafolio)
		AND	MOV."IdEstado" = 'PEN'
		AND MOV."EsVentaDescubierta" = 'S'
		AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
		AND
			(
				TipoOperacion."AccionCustodia" = 'RETIRO' AND  MOV."EsNominal" = 'N'
			)
		AND	COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(:p_IdClasificacionTipoInstrumento,'') <> '' THEN :p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") END
		AND	COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") = CASE WHEN COALESCE(:p_IdTipoInstrumento,'') <> '' THEN :p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") END
		AND	COALESCE(rel."IdInstrumento",MOV."IdInstrumento") IN (:p_IdInstrumento)
		AND	COALESCE(rel."Nemotecnico", MOV."Nemotecnico") = CASE WHEN COALESCE(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE COALESCE(rel."Nemotecnico", MOV."Nemotecnico") END
		AND MOV."FechaOperacion" <= :v_FechaConsulta
    	AND MOV."FechaLiquidacion" > :v_FechaConsulta
		AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
		AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
		AND MOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV', 'PRESTA','EMPRES')
		
)
