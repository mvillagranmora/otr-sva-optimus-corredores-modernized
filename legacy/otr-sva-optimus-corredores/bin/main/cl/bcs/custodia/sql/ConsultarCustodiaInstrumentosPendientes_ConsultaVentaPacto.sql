SELECT 
    SUB."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
   ,SUB."IdTipoInstrumento" as "IdTipoInstrumento"
   ,SUB."IdInstrumento" as "IdInstrumento"
   ,case when SUB."IdClasificacionTipoInstrumento" = 'IF' then SUB."NemoSVS" else SUB."Nemotecnico" end as "Nemotecnico"
   ,SUB."IdEmisor" as "IdEmisor"
   ,case when SUB."IdClasificacionTipoInstrumento"= 'IF' then SUB."EsFungible" else 'N' end as "EsFungible"
   ,CASE WHEN :p_IdCliente > 0 AND :p_IdCliente = :v_IdClienteCuentaPropia THEN CAR."IdClienteRelacionado" ELSE CAR."IdCliente" END as "IdCliente"
   ,CASE WHEN :p_IdCliente > 0 AND :p_IdCliente = :v_IdClienteCuentaPropia THEN CAR."IdPortafolioRelacionado" ELSE CAR."IdPortafolio" END as "IdPortafolio"
   ,COALESCE(MADOR."NombreProductoAgrupador", 'Otros (No APV)') AS "Producto"
   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN :v_IdBovedaDefecto ELSE RES."IdBoveda" END AS "IdBoveda"
   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN  0 ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio",0) END AS "IdCuentaCustodioPortafolio"
   ,CASE WHEN RES."FolioCarteraOrigen"!=0 THEN 
   		CASE WHEN SUB."IdClasificacionTipoInstrumento"= 'IF' THEN SUB."INGValorRescate" ELSE SUB."INGCantidad" END 
   ELSE 0 END 	AS "Cantidad" 
   	
   ,CASE WHEN RES."FolioCarteraOrigen"=0 THEN 
   		CASE WHEN SUB."IdClasificacionTipoInstrumento"= 'IF' THEN SUB."INGValorRescate" ELSE SUB."INGCantidad" END 
   ELSE 0 END	AS "CantidadNominal" 
   ,COALESCE(SUB."INGPrecioSubyacente",0) as "PrecioCompra" 						
   ,COALESCE(SUB."INGMontoATasaSubyacenteActual",0) as "MontoCompra" 				
   ,COALESCE(SUB."INGMontoSubyacenteActualMonedaInstrumento",0) as "ValorCompra" 	
   ,COALESCE(SUB."INGPrecioMercadoActual",0) as "INGPrecioMercadoActual" 			
   ,COALESCE(SUB."INGTasaMercadoActual",0) as "INGTasaMercadoActual" 				
   ,COALESCE(SUB."INGMontoMercadoActualMonedaInstrumento",0) as "ValorMercado"
   ,COALESCE(SUB."INGMontoATasaMercadoActual",0) as "MontoMercado"
   ,CASE
   		WHEN ifnull(SUB."IdTipoReajusteSubyacente",'') <> '' THEN SUB."IdTipoReajusteSubyacente"
   		WHEN ifnull(SUB."IdMonedaInstrumento",'') <> '' THEN SUB."IdMonedaInstrumento"
   		ELSE ifnull(CAR."IdMonedaSistema",'')
   	END AS "IdMonedaInstrReajuste"
   ,CASE
   		WHEN IFNULL(SUB."IdMonedaEmision",'') <> '' THEN SUB."IdMonedaEmision"
   		WHEN IFNULL(SUB."IdTipoReajusteSubyacente",'') <> '' THEN SUB."IdTipoReajusteSubyacente"
   		ELSE SUB."IdMonedaInstrumento" 
  	END AS "IdMonedaEmision"
  ,SUB."IdMonedaSistema" AS "IdMonedaPrincipal"
  ,COALESCE(SUB."INGMontoInteresCorridoSubyacenteActual",0) AS "InteresCorrido"
  ,CASE WHEN RES."FolioCarteraOrigen"=0 THEN 'S' ELSE 'N' END AS "EsNominal"  
  ,CASE WHEN COALESCE(POR."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(POR."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
  ,CASE WHEN COALESCE(POR."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MT."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
  ,CLI."IdPersona"
  ,inst3."RazonSocial" AS "RazonSocialInstrumento"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= SUB."FolioSubyacente" 
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = :p_IdEmpresa AND CLI."IdCliente" = CASE WHEN :p_IdCliente > 0 AND :p_IdCliente = :v_IdClienteCuentaPropia THEN CAR."IdClienteRelacionado" ELSE CAR."IdCliente" END)
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = CAR."IdPortafolio" AND POR."IdCliente" = CAR."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MT ON MT."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = POR."CodigoPortafolio" AND  POR."IdMatrizAgrupadorAPV" = 0
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MT."IdProductoAgrupador"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SUB."IdInstrumento"
WHERE CAR."IdEmpresa" = :p_IdEmpresa
AND CAR."IdEstado" = 'VIG'
AND	CASE WHEN :p_IdCliente > 0 AND :p_IdCliente = :v_IdClienteCuentaPropia THEN CAR."IdClienteRelacionado" ELSE CAR."IdCliente" END = case when :p_IdCliente > 0 then :p_IdCliente else CAR."IdCliente" END
AND	CASE WHEN :p_IdCliente > 0 AND :p_IdCliente = :v_IdClienteCuentaPropia THEN CAR."IdPortafolioRelacionado" ELSE CAR."IdPortafolio" END IN (:p_IdPortafolio)
AND
(
       (:p_TipoFechaConsulta = 'LIQ' AND CAR."INGFechaLiquidacion" <= :v_FechaConsulta)
    OR (:p_TipoFechaConsulta = 'ADQ' AND CAR."INGFechaOperacion" <= :v_FechaConsulta)
)
AND	SUB."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(:p_IdClasificacionTipoInstrumento,'') <> '' THEN :p_IdClasificacionTipoInstrumento ELSE SUB."IdClasificacionTipoInstrumento" END
AND	SUB."IdTipoInstrumento" = CASE WHEN IFNULL(:p_IdTipoInstrumento,'') <> '' THEN :p_IdTipoInstrumento ELSE SUB."IdTipoInstrumento" END
AND	SUB."IdInstrumento"    IN (:p_IdInstrumento)
AND SUB."Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE SUB."Nemotecnico" END
AND CAR."IdTipoInstrumento" = 'PAC'
AND SUB."IdEstado"='VIG'
AND CAR."IdTipoOperacion"   = CASE WHEN :p_IdCliente = :v_IdClienteCuentaPropia THEN 'VENPAC' ELSE 'COMPAC' END
AND (:p_IdProductoAgrupador IS NULL OR MAP(MT."IdProductoAgrupador",NULL,0,MT."IdProductoAgrupador") = :p_IdProductoAgrupador)
UNION ALL
SELECT 
    SUB."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
   ,SUB."IdTipoInstrumento" as "IdTipoInstrumento"
   ,SUB."IdInstrumento" as "IdInstrumento"
   ,case when SUB."IdClasificacionTipoInstrumento" = 'IF' then SUB."NemoSVS" else SUB."Nemotecnico" end as "Nemotecnico"
   ,SUB."IdEmisor" as "IdEmisor"
   ,case when SUB."IdClasificacionTipoInstrumento"= 'IF' then SUB."EsFungible" else 'N' end as "EsFungible"
   ,CAR."IdClienteRelacionado" as "IdCliente"
   ,CAR."IdPortafolioRelacionado" as "IdPortafolio"
   ,COALESCE(MADOR."NombreProductoAgrupador", 'Otros (No APV)') AS "Producto"
   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN :v_IdBovedaDefecto ELSE RES."IdBoveda" END AS "IdBoveda"
   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN  0 ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio",0) END AS "IdCuentaCustodioPortafolio"
   ,CASE WHEN RES."FolioCarteraOrigen"!=0 THEN 
   		CASE WHEN SUB."IdClasificacionTipoInstrumento"= 'IF' THEN SUB."INGValorRescate" ELSE SUB."INGCantidad" END 
   ELSE 0 END 	AS "Cantidad" 
   	
   ,CASE WHEN RES."FolioCarteraOrigen"=0 THEN 
   		CASE WHEN SUB."IdClasificacionTipoInstrumento"= 'IF' THEN SUB."INGValorRescate" ELSE SUB."INGCantidad" END 
   ELSE 0 END	AS "CantidadNominal" 
   ,COALESCE(SUB."INGPrecioSubyacente",0) as "PrecioCompra" 						
   ,COALESCE(SUB."INGMontoATasaSubyacenteActual",0) as "MontoCompra" 				
   ,COALESCE(SUB."INGMontoSubyacenteActualMonedaInstrumento",0) as "ValorCompra" 	
   ,COALESCE(SUB."INGPrecioMercadoActual",0) as "INGPrecioMercadoActual" 			
   ,COALESCE(SUB."INGTasaMercadoActual",0) as "INGTasaMercadoActual" 				
   ,COALESCE(SUB."INGMontoMercadoActualMonedaInstrumento",0) as "ValorMercado"
   ,COALESCE(SUB."INGMontoATasaMercadoActual",0) as "MontoMercado"
   ,CASE
   		WHEN ifnull(SUB."IdTipoReajusteSubyacente",'') <> '' THEN SUB."IdTipoReajusteSubyacente"
   		WHEN ifnull(SUB."IdMonedaInstrumento",'') <> '' THEN SUB."IdMonedaInstrumento"
   		ELSE ifnull(CAR."IdMonedaSistema",'')
   	END AS "IdMonedaInstrReajuste"
   ,CASE
   		WHEN IFNULL(SUB."IdMonedaEmision",'') <> '' THEN SUB."IdMonedaEmision"
   		WHEN IFNULL(SUB."IdTipoReajusteSubyacente",'') <> '' THEN SUB."IdTipoReajusteSubyacente"
   		ELSE SUB."IdMonedaInstrumento" 
  	END AS "IdMonedaEmision"
  ,SUB."IdMonedaSistema" AS "IdMonedaPrincipal"
  ,COALESCE(SUB."INGMontoInteresCorridoSubyacenteActual",0) AS "InteresCorrido"
  ,CASE WHEN RES."FolioCarteraOrigen"=0 THEN 'S' ELSE 'N' END AS "EsNominal"  
  ,CASE WHEN COALESCE(POR."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(POR."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
  ,CASE WHEN COALESCE(POR."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MT."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
  ,CLI."IdPersona"
  ,inst3."RazonSocial" AS "RazonSocialInstrumento"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= SUB."FolioSubyacente" 
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = :p_IdEmpresa AND CLI."IdCliente" = CASE WHEN :p_IdCliente > 0 AND :p_IdCliente = :v_IdClienteCuentaPropia THEN CAR."IdClienteRelacionado" ELSE CAR."IdCliente" END)
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = CAR."IdPortafolio" AND POR."IdCliente" = CAR."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MT ON MT."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = POR."CodigoPortafolio" AND  POR."IdMatrizAgrupadorAPV" = 0
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MT."IdProductoAgrupador"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SUB."IdInstrumento"
WHERE CAR."IdEmpresa" = :p_IdEmpresa
AND CAR."IdEstado" = 'VIG'
AND	CAR."IdClienteRelacionado" = case when :p_IdCliente = 0 then :v_IdClienteCuentaPropia else -1 END--solo cuando combobox Cliente vacio
AND	CAR."IdPortafolioRelacionado" IN (:p_IdPortafolio)
AND
(
       (:p_TipoFechaConsulta = 'LIQ' AND CAR."INGFechaLiquidacion" <= :v_FechaConsulta)
    OR (:p_TipoFechaConsulta = 'ADQ' AND CAR."INGFechaOperacion" <= :v_FechaConsulta)
)
AND	SUB."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(:p_IdClasificacionTipoInstrumento,'') <> '' THEN :p_IdClasificacionTipoInstrumento ELSE SUB."IdClasificacionTipoInstrumento" END
AND	SUB."IdTipoInstrumento" = CASE WHEN IFNULL(:p_IdTipoInstrumento,'') <> '' THEN :p_IdTipoInstrumento ELSE SUB."IdTipoInstrumento" END
AND	SUB."IdInstrumento"      IN (:p_IdInstrumento)
AND SUB."Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE SUB."Nemotecnico" END
AND CAR."IdTipoInstrumento" = 'PAC'
AND SUB."IdEstado"='VIG'
AND CAR."IdTipoOperacion"   = 'VENPAC'
AND (:p_IdProductoAgrupador IS NULL OR MAP(MT."IdProductoAgrupador",NULL,0,MT."IdProductoAgrupador") = :p_IdProductoAgrupador)
