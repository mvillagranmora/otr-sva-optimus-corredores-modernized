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
   ,"IdTipoOperacion"
   ,"AccionCustodia"
   ,"IdMatrizAgrupador"
   ,"EsADC"
   ,"IdPersona"
   ,"RazonSocialInstrumento"
FROM
(

SELECT
       SIM."suby_IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento", SIM."suby_IdTipoInstrumento" as "IdTipoInstrumento", SIM."suby_IdInstrumento" as "IdInstrumento"
       ,case when "suby_IdClasificacionTipoInstrumento" = 'IF' then SIM."suby_NemoSVS" else SIM."suby_Nemotecnico" end as "Nemotecnico"
       ,SIM."suby_IdEmisor" as "IdEmisor"
       ,case when "suby_IdClasificacionTipoInstrumento" = 'IF' then SIM."suby_EsFungible" else 'N' end as "EsFungible"
       ,SIM."IdCliente"
       ,SIM."IdPortafolio"
       ,COALESCE(MADOR."NombreProductoAgrupador", 'Otros (No APV)') AS "Producto"
       ,CASE WHEN :p_AgrupaPorBoveda='N' THEN :v_IdBovedaDefecto ELSE SIM."suby_IdBoveda" END AS "IdBoveda" 
       ,CASE WHEN :p_AgrupaPorBoveda='N' THEN  0 ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio",0) END AS "IdCuentaCustodioPortafolio"
       ,cast(IFNULL(case when "suby_IdClasificacionTipoInstrumento" = 'IF' then SIM."INGValorRescate" else SIM."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
       ,COALESCE(SIM."suby_INGPrecioCompra",0) as "PrecioCompra"                            
       ,COALESCE(SIM."suby_INGMontoATasaCompraActual",0) as "MontoCompra"                   
       ,COALESCE(SIM."suby_INGMontoATasaCompraActualMonedaInstrumento",0) as "ValorCompra"  
       ,COALESCE(SIM."suby_INGPrecioMercadoActual",0) as "INGPrecioMercadoActual"           
       ,COALESCE(SIM."suby_INGTasaMercadoActual",0) as "INGTasaMercadoActual"               
       ,COALESCE(SIM."suby_INGMontoMercadoActualMonedaInstrumento",0) as "ValorMercado"
       ,COALESCE(SIM."suby_INGMontoATasaMercadoActual",0) as "MontoMercado"
       ,CASE
            WHEN ifnull(SIM."suby_IdMonedaInstrumento",'') <> '' THEN SIM."suby_IdMonedaInstrumento"
            WHEN ifnull(SIM."suby_IdTipoReajusteSubyacente",'') <> '' THEN SIM."suby_IdTipoReajusteSubyacente"
            ELSE ifnull(SIM."IdMonedaSistema",'')
        END AS "IdMonedaInstrReajuste"
       ,CASE
            WHEN IFNULL(SIM."suby_EsPagaderoEn",'') <> '' THEN IFNULL(SIM."suby_EsPagaderoEn",'') 
            WHEN ifnull(SIM."suby_IdMonedaInstrumento",'') <> '' THEN SIM."suby_IdMonedaInstrumento"
            WHEN ifnull(SIM."suby_IdTipoReajusteSubyacente",'') <> '' THEN SIM."suby_IdTipoReajusteSubyacente"
            ELSE ifnull(SIM."IdMonedaSistema",'')
        END AS "IdMonedaEmision"
       ,SIM."IdMonedaSistema" as "IdMonedaPrincipal"
       ,SIM."INGMontoInteresCorridoCompraActual" AS "InteresCorrido"
       ,SIM."IdTipoOperacion"
       ,SIM."AccionOperacion" AS "AccionCustodia"
       ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
       ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
       ,CLI."IdPersona"
       ,inst3."RazonSocial" AS "RazonSocialInstrumento"
    FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultaneaHis" AS SIM
    LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =:p_IdEmpresa AND CLI."IdCliente" = SIM."IdCliente")
    LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = SIM."IdPortafolio" AND Portafolio."IdCliente" = SIM."IdCliente"
    LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
    LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = SIM."IdCuentaCustodioPortafolio"
    LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
    LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SIM."suby_IdInstrumento"
    WHERE
        SIM."IdEmpresa" =:p_IdEmpresa
    AND	SIM."FechaRespaldo" = :v_FechaConsulta
    AND SIM."IdEstado" = 'VIG'
    AND SIM."IdCliente" = case when:p_IdCliente > 0 then:p_IdCliente else SIM."IdCliente" END
    AND SIM."IdPortafolio" 	IN (:p_IdPortafolio)
    AND SIM."IdTipoOperacion" IN ('VENSIM','COMSIM')  --old: COMSIM
    AND
	(
	       (:p_TipoFechaConsulta = 'LIQ' AND SIM."INGFechaLiquidacion" <= :v_FechaConsulta)
	    OR (:p_TipoFechaConsulta = 'ADQ' AND SIM."INGFechaOperacion" <= :v_FechaConsulta)
	)
    AND SIM."suby_IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(:p_IdClasificacionTipoInstrumento,'') <> '' THEN:p_IdClasificacionTipoInstrumento ELSE SIM."suby_IdClasificacionTipoInstrumento" END
    AND SIM."suby_IdTipoInstrumento" = CASE WHEN IFNULL(:p_IdTipoInstrumento,'') <> '' THEN:p_IdTipoInstrumento ELSE SIM."suby_IdTipoInstrumento" END
    AND SIM."suby_IdInstrumento" IN (:p_IdInstrumento) 
    AND SIM."suby_Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE SIM."suby_Nemotecnico" END
    AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
)
