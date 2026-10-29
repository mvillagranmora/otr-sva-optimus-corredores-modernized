SELECT
       CAR."IdClasificacionTipoInstrumento"
       ,CAR."IdTipoInstrumento"
       ,CAR."IdInstrumento"
       ,Case when CAR."IdClasificacionTipoInstrumento" = 'IF' then CAR."NemoSVS" else CAR."Nemotecnico" end as "Nemotecnico"
       ,CASE WHEN coalesce(CAR."IdEmisor",inst3."IdEmisor",'') <> '' THEN coalesce(CAR."IdEmisor",inst3."IdEmisor",'') ELSE inst3."IdEmisor" END AS "IdEmisor"
       ,case when CAR."IdClasificacionTipoInstrumento" = 'IF' then CAR."EsFungible" else 'N' end as "EsFungible"
       ,CAR."IdCliente"
       ,CLI."IdPersona"
       ,CAR."IdPortafolio"
       ,COALESCE(MADOR."NombreProductoAgrupador", 'Otros (No APV)') AS "Producto"
       ,CASE WHEN :p_AgrupaPorBoveda='N' THEN :v_IdBovedaDefecto ELSE COALESCE(CAR."IdBoveda",:v_IdBovedaDefecto) END AS "IdBoveda" 
       ,CASE WHEN :p_AgrupaPorBoveda='N' THEN  0 ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio",0) END AS "IdCuentaCustodioPortafolio"
       ,CASE WHEN :p_AgrupaPorBoveda='N' THEN '' ELSE COALESCE(CCPI."Codigo",'') END AS "NumeroCuentaBoveda"      
        ,CAST(IFNULL(CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN  
       		CAR."INGValorRescate" - COALESCE (resTot."IngValorRescate",0)
       	ELSE 
       		CAR."INGCantidad"- COALESCE (resTot."IngCantidad",0)  END,0) AS DECIMAL(26,8)) AS "Cantidad"
       ,CAR."INGCantidad" AS "CantidadCartera"
       ,resTot."IngCantidad" AS "CantidadReserva"
       ,CAR."IdCartera"
       ,COALESCE (CAR."INGPrecioCompra",0) as "PrecioCompra"
       ,COALESCE (CAR."INGMontoATasaCompraActual",0) as "MontoCompra"                   
       ,COALESCE (CAR."INGMontoCompraActualMonedaInstrumento",0) as "ValorCompra"       
       ,COALESCE (CAR."INGPrecioMercadoActual",0) as "INGPrecioMercadoActual"           
       ,COALESCE (CAR."INGTasaMercadoActual",0) as "INGTasaMercadoActual"           
       ,COALESCE (CAR."INGMontoMercadoActualMonedaInstrumento",0) as "ValorMercado"
       ,COALESCE (CAR."INGMontoATasaMercadoActual",0) as "MontoMercado"
       ,CASE
            WHEN COALESCE(CAR."IdTipoReajuste",inst3."IdTipoReajuste", '') <> '' THEN COALESCE(CAR."IdTipoReajuste",inst3."IdTipoReajuste", '')
            WHEN ifnull(CAR."IdMonedaPrincipal",'') <> '' THEN CAR."IdMonedaPrincipal"
            ELSE COALESCE(CAR."IdMonedaInstrumento",inst3."IdMoneda",'')
        END AS "IdMonedaInstrReajuste"
       ,CASE
            WHEN IFNULL(CAR."IdMonedaEmision",'') <> '' THEN CAR."IdMonedaEmision"
            WHEN IFNULL(CAR."IdTipoReajuste",'') <> '' THEN CAR."IdTipoReajuste"
            ELSE inst3."IdMoneda"
        END AS "IdMonedaEmision"
       ,CASE 
            WHEN IFNULL(CAR."IdMonedaPrincipal",'') <> '' THEN IFNULL(CAR."IdMonedaPrincipal",'')
            WHEN COALESCE(CAR."IdTipoReajuste",inst3."IdTipoReajuste", '') <> '' THEN COALESCE(CAR."IdTipoReajuste",inst3."IdTipoReajuste", '')
            ELSE COALESCE(CAR."IdMonedaInstrumento",inst3."IdMoneda",'')
        END AS "IdMonedaPrincipal"
       ,COALESCE (CAR."INGMontoInteresCorridoCompraActual",0) AS "InteresCorrido"  
       ,COALESCE (REG."Descripcion",'') AS "RegimenTributario"
       ,COALESCE (CAR."INGPrecioPromedioCompra",0) AS "INGPrecioPromedioCompra"
       ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
       ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
       ,CAR."IdTipoOperacion"
       ,CAR."ValorFactorMonedaInstrumento"
       ,COALESCE(REG."IdRegimenFondoMutuo",'') AS "IdRegimenTributario"
       ,inst3."RazonSocial" AS "RazonSocialInstrumento"
    FROM "BSCL.Custodia.BD::HIS.Cartera" AS CAR
    LEFT JOIN (
	SELECT RES."FolioCarteraOrigen"
		,SUM("IngCantidad") AS "IngCantidad"
		,SUM("IngValorRescate") AS "IngValorRescate"
		,SUM("MontoValorizado") AS "IngMontoValorizado"
	FROM "BSCL.Custodia.BD::CUS.Reserva" AS RES 
	WHERE RES."IdEstado"='VIG' AND RES."EgrCantidad" = 0
	GROUP BY RES."FolioCarteraOrigen") AS resTot ON resTot."FolioCarteraOrigen" = CAR."FolioCartera"       
    LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = :p_IdEmpresa AND CLI."IdCliente" = CAR."IdCliente")  
    LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
    LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
    LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = CAR."IdInstrumento"
    LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = CAR."IdRegimenFondoMutuo"
    LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
    LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
    WHERE
        CAR."IdEmpresa" = :p_IdEmpresa
    AND CAR."FechaRespaldo" = :v_FechaConsulta
    AND CAR."IdEstado" = 'VIG'
    AND CAR."IdCliente" = :p_IdCliente
    AND CAR."IdTipoInstrumento" NOT IN ('PRT','EMP','SIM','PAC','REP','RRV','RRF')
    AND CAR."FechaOperacion" <= :v_FechaConsulta
    AND CAR."INGFechaLiquidacion" > :v_FechaConsulta
    <<CASE.CAR.p_IdPortafolio>>
    <<CASE.CAR.p_IdClasificacionTipoInstrumento>>
    <<CASE.CAR.p_IdTipoInstrumento>>
    <<CASE.CAR.p_IdInstrumento>>
    <<CASE.CAR.p_Nemotecnico>>