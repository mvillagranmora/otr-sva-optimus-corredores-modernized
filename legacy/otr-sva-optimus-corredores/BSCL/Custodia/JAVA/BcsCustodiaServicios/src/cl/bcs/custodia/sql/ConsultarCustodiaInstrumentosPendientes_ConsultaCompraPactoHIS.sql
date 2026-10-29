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
   ,CASE WHEN :p_IdCliente > 0 AND :p_IdCliente = :v_IdClienteCuentaPropia THEN PAC."IdClienteRelacionado" ELSE PAC."IdCliente" END as "IdCliente"
   ,CASE WHEN :p_IdCliente > 0 AND :p_IdCliente = :v_IdClienteCuentaPropia THEN PAC."IdPortafolioRelacionado" ELSE PAC."IdPortafolio" END as "IdPortafolio"
   ,COALESCE(MADOR."NombreProductoAgrupador", 'Otros (No APV)') AS "Producto"
   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN :v_IdBovedaDefecto ELSE PAC."IdBoveda" END AS "IdBoveda" 
   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN  0 ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio",0) END AS "IdCuentaCustodioPortafolio"
   ,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."INGValorRescate" else sby."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
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
   ,CASE
   		WHEN IFNULL(sby."IdMonedaEmision",'') <> '' THEN sby."IdMonedaEmision"
   		WHEN IFNULL(sby."IdTipoReajusteSubyacente",'') <> '' THEN sby."IdTipoReajusteSubyacente"
   		ELSE sby."IdMonedaInstrumento"
	END AS "IdMonedaEmision"
   ,sby."IdMonedaSistema" AS "IdMonedaPrincipal"
   ,sby."INGMontoInteresCorridoSubyacenteActual" AS "InteresCorrido"
   ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
   ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
   ,CLI."IdPersona"
   ,inst3."RazonSocial" AS "RazonSocialInstrumento"
FROM "BSCL.Custodia.BD::HIS.Cartera" AS PAC
INNER JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarUltimoMovimientoSubyacente" as ultSub on (PAC."FolioCartera" = ultSub."FolioCarteraRelacionada" AND ultSub."FechaRespaldo" = PAC."FechaRespaldo")
INNER JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarteraSubyacenteBasica" as sby on (sby."IdSubyacente" = ultSub."IdSubyacente" AND sby."FechaRespaldo" = PAC."FechaRespaldo")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =:p_IdEmpresa AND CLI."IdCliente" = CASE WHEN:p_IdCliente > 0 AND:p_IdCliente = :v_IdClienteCuentaPropia THEN PAC."IdClienteRelacionado" ELSE PAC."IdCliente" END)			
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PAC."IdPortafolio" AND Portafolio."IdCliente" = PAC."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PAC."IdCuentaCustodioPortafolio"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = sby."IdInstrumento"
WHERE
	PAC."IdEmpresa" =:p_IdEmpresa
AND	PAC."FechaRespaldo" = :v_FechaConsulta
AND PAC."IdEstado" = 'VIG'
AND	CASE WHEN:p_IdCliente > 0 AND:p_IdCliente = :v_IdClienteCuentaPropia THEN PAC."IdClienteRelacionado" ELSE PAC."IdCliente" END = case when:p_IdCliente > 0 then:p_IdCliente else PAC."IdCliente" END
AND	CASE WHEN:p_IdCliente > 0 AND:p_IdCliente = :v_IdClienteCuentaPropia THEN PAC."IdPortafolioRelacionado" ELSE PAC."IdPortafolio" END IN (:p_IdPortafolio)
AND
(
       (:p_TipoFechaConsulta = 'LIQ' AND PAC."INGFechaLiquidacion" <= :v_FechaConsulta)
    OR (:p_TipoFechaConsulta = 'ADQ' AND PAC."INGFechaOperacion" <= :v_FechaConsulta)
)
AND	sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(:p_IdClasificacionTipoInstrumento,'') <> '' THEN:p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
AND	sby."IdTipoInstrumento" = CASE WHEN IFNULL(:p_IdTipoInstrumento,'') <> '' THEN:p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
AND	sby."IdInstrumento"IN (:p_IdInstrumento)
AND sby."Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE sby."Nemotecnico" END
AND PAC."IdTipoInstrumento" = 'PAC'
AND PAC."IdTipoOperacion" = CASE WHEN:p_IdCliente = :v_IdClienteCuentaPropia THEN 'COMPAC' ELSE 'VENPAC' END
AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
UNION ALL
SELECT
	sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
   ,sby."IdInstrumento" as "IdInstrumento"
   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
   ,sby."IdEmisor" as "IdEmisor"
   ,case when sby."IdClasificacionTipoInstrumento"= 'IF' then sby."EsFungible" else 'N' end as "EsFungible"
   ,PAC."IdClienteRelacionado" as "IdCliente"
   ,PAC."IdPortafolioRelacionado" as "IdPortafolio"
   ,COALESCE(MADOR."NombreProductoAgrupador", 'Otros (No APV)') AS "Producto"
   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN :v_IdBovedaDefecto ELSE PAC."IdBoveda" END AS "IdBoveda" 
   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN  0 ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio",0) END AS "IdCuentaCustodioPortafolio"
   ,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."INGValorRescate" else sby."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
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
   ,CASE
   		WHEN IFNULL(sby."IdMonedaEmision",'') <> '' THEN sby."IdMonedaEmision"
   		WHEN IFNULL(sby."IdTipoReajusteSubyacente",'') <> '' THEN sby."IdTipoReajusteSubyacente"
   		ELSE sby."IdMonedaInstrumento"
	END AS "IdMonedaEmision"
   ,sby."IdMonedaSistema" AS "IdMonedaPrincipal"
   ,sby."INGMontoInteresCorridoSubyacenteActual" AS "InteresCorrido"
   ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
   ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
   ,CLI."IdPersona"
   ,inst3."RazonSocial" AS "RazonSocialInstrumento"
FROM "BSCL.Custodia.BD::HIS.Cartera" AS PAC
INNER JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarUltimoMovimientoSubyacente" as ultSub on (PAC."FolioCartera" = ultSub."FolioCarteraRelacionada" AND ultSub."FechaRespaldo" = PAC."FechaRespaldo")
INNER JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarteraSubyacenteBasica" as sby on (sby."IdSubyacente" = ultSub."IdSubyacente" AND sby."FechaRespaldo" = PAC."FechaRespaldo")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =:p_IdEmpresa AND CLI."IdCliente" = CASE WHEN:p_IdCliente > 0 AND:p_IdCliente = :v_IdClienteCuentaPropia THEN PAC."IdClienteRelacionado" ELSE PAC."IdCliente" END)			
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PAC."IdPortafolio" AND Portafolio."IdCliente" = PAC."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PAC."IdCuentaCustodioPortafolio"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = sby."IdInstrumento"
WHERE
	PAC."IdEmpresa" =:p_IdEmpresa
AND	PAC."FechaRespaldo" = :v_FechaConsulta
AND PAC."IdEstado" = 'VIG'
AND	PAC."IdClienteRelacionado" = case when :p_IdCliente = 0 then :v_IdClienteCuentaPropia else -1 END--solo cuando combobox Cliente vacio
AND	PAC."IdPortafolioRelacionado" IN (:p_IdPortafolio)
AND 
(
       (:p_TipoFechaConsulta = 'LIQ' AND PAC."INGFechaLiquidacion" <= :v_FechaConsulta)
    OR (:p_TipoFechaConsulta = 'ADQ' AND PAC."INGFechaOperacion" <= :v_FechaConsulta)
)
AND	sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(:p_IdClasificacionTipoInstrumento,'') <> '' THEN:p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
AND	sby."IdTipoInstrumento" = CASE WHEN IFNULL(:p_IdTipoInstrumento,'') <> '' THEN:p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
AND	sby."IdInstrumento" IN (:p_IdInstrumento) 
AND sby."Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE sby."Nemotecnico" END
AND PAC."IdTipoInstrumento" = 'PAC'
AND PAC."IdTipoOperacion" = 'COMPAC'
AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
)