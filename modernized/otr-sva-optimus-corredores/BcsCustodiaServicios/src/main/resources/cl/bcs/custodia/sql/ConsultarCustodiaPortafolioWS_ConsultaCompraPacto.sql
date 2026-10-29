-- Optimizacion: se reemplaza el LEFT JOIN a la vista "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica"
-- (~200 columnas, 7 joins, ORDER BY en la propia definicion de la vista) por una subconsulta acotada a las
-- columnas realmente consumidas por este servicio, sin ORDER BY y sin los joins que no aportan a esas columnas
-- (INS.TipoInstrumento, INS.TipoInstrumentoClasificacionRiesgo, INS.InstrumentoValorClasificacionRiesgo).
-- Se corrige ademas el join a PLA.Configuracion: la vista lo hace contra origen."IdEmpresa" (NULL cuando el
-- subyacente no tiene cartera de origen), aqui se usa :p_IdEmpresa directo.
WITH SUB_BASICA AS (
    SELECT
         suby."FolioCarteraRelacionada"
        ,suby."FolioSubyacente"
        ,suby."IdEstado"
        ,suby."IdClasificacionTipoInstrumento"
        ,suby."IdTipoInstrumento"
        ,suby."IdInstrumento"
        ,suby."Nemotecnico"
        ,suby."NemoSVS"
        ,CASE WHEN coalesce(suby."IdEmisor",Inst."IdEmisor",'') <> '' THEN coalesce(suby."IdEmisor",Inst."IdEmisor",'') ELSE Inst."IdEmisor" END AS "IdEmisor"
        ,suby."EsFungible"
        ,IFNULL(suby."IdTipoReajuste",'') AS "IdTipoReajusteSubyacente"
        ,CASE WHEN IFNULL(suby."IdMonedaInstrumento",'')<>'' THEN IFNULL(suby."IdMonedaInstrumento",'') ELSE COALESCE(Inst."IdMoneda",'') END AS "IdMonedaInstrumento"
        ,CASE WHEN IFNULL(suby."IdMonedaEmision",'')<>'' THEN IFNULL(suby."IdMonedaEmision",'') ELSE COALESCE(Inst."IdMoneda",'') END AS "IdMonedaEmision"
        ,CASE WHEN IFNULL(origen."IdMonedaSistema",'') <> '' THEN IFNULL(origen."IdMonedaSistema",'') ELSE IFNULL(conf."Valor",'CLP') END AS "IdMonedaSistema"
        ,suby."INGCantidad"
        ,suby."INGValorRescate"
        ,suby."INGPrecioSubyacente"
        ,suby."INGMontoATasaSubyacenteActual"
        ,suby."INGMontoSubyacenteActualMonedaInstrumento"
        ,suby."INGPrecioMercadoActual"
        ,suby."INGTasaMercadoActual"
        ,suby."INGMontoMercadoActualMonedaInstrumento"
        ,suby."INGMontoATasaMercadoActual"
        ,suby."INGMontoInteresCorridoSubyacenteActual"
    FROM "BSCL.Custodia.BD::CUS.Subyacente" AS suby
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Inst ON suby."IdInstrumento" = Inst."IdInstrumento"
    LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS origen ON (origen."IdCartera" = suby."IdCarteraRetirada")
    LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS conf ON (conf."IdEmpresa" = :p_IdEmpresa AND conf."CodigoParametro" = 'PLA.MonedaSistema')
)
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
   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN '' ELSE COALESCE(CCPI."Codigo",'') END AS "NumeroCuentaBoveda"			   
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
  ,CASE 
        	WHEN IFNULL(CAR."IdMonedaInstrumento",'') = 'UF'
        		THEN 'CLP'
        	ELSE 
        		CAR."IdMonedaInstrumento"
        END          AS "EsPagaderoEn"
  ,COALESCE(SUB."INGMontoInteresCorridoSubyacenteActual",0) AS "InteresCorrido"
  ,CASE WHEN RES."FolioCarteraOrigen"=0 THEN 'S' ELSE 'N' END AS "EsNominal" 
  ,CASE WHEN COALESCE(POR."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(POR."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
  ,CASE WHEN COALESCE(POR."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MT."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
  ,CLI."IdPersona"
  ,COALESCE(SUB."INGMontoATasaSubyacenteActual",0) AS "MontoCompraInicial"
  ,inst3."RazonSocial" AS "RazonSocialInstrumento"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
LEFT JOIN SUB_BASICA AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= SUB."FolioSubyacente" 
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = :p_IdEmpresa AND CLI."IdCliente" = CASE WHEN :p_IdCliente > 0 AND :p_IdCliente = :v_IdClienteCuentaPropia THEN CAR."IdClienteRelacionado" ELSE CAR."IdCliente" END)
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = CAR."IdPortafolio" AND POR."IdCliente" = CAR."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MT ON MT."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = POR."CodigoPortafolio" AND POR."IdMatrizAgrupadorAPV" = 0
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MT."IdProductoAgrupador"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SUB."IdInstrumento"
WHERE CAR."IdEmpresa" = :p_IdEmpresa
AND CAR."IdEstado" = 'VIG'
AND	CASE WHEN  :p_IdCliente > 0 AND :p_IdCliente = :v_IdClienteCuentaPropia THEN CAR."IdClienteRelacionado" ELSE CAR."IdCliente" END = case when :p_IdCliente > 0 then :p_IdCliente else CAR."IdCliente" END
AND	CASE WHEN  :p_IdCliente > 0 AND :p_IdPortafolio = :v_IdPortafolioCuentaPropia THEN CAR."IdPortafolioRelacionado" ELSE CAR."IdPortafolio" END = case WHEN  :p_IdPortafolio > 0 THEN  :p_IdPortafolio else CAR."IdPortafolio" END
<<FECHACONSULTA.CAR.p_TipoFechaConsulta>>
<<CASE.SUB.p_IdClasificacionTipoInstrumento>>
<<CASE.SUB.p_IdTipoInstrumento>>
<<CASE.SUB.p_IdInstrumento>>
<<CASE.SUB.p_Nemotecnico>> 
AND CAR."IdTipoInstrumento" = 'PAC'
AND CAR."IdTipoOperacion"   = CASE WHEN :p_IdCliente = :v_IdClienteCuentaPropia THEN 'COMPAC' ELSE 'VENPAC' END
AND SUB."IdEstado"='VIG'
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
   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN '' ELSE COALESCE(CCPI."Codigo",'') END AS "NumeroCuentaBoveda"			   
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
        	WHEN IFNULL(CAR."IdMonedaInstrumento",'') = 'UF'
        		THEN 'CLP'
        	ELSE 
        		CAR."IdMonedaInstrumento"
        END          AS "EsPagaderoEn"
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
  ,COALESCE(SUB."INGMontoATasaSubyacenteActual",0) AS "MontoCompraInicial"
  ,inst3."RazonSocial" AS "RazonSocialInstrumento"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
LEFT JOIN SUB_BASICA AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= SUB."FolioSubyacente" 
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = :p_IdEmpresa AND CLI."IdCliente" = CASE WHEN :p_IdCliente > 0 AND :p_IdCliente = :v_IdClienteCuentaPropia THEN CAR."IdClienteRelacionado" ELSE CAR."IdCliente" END)
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = CAR."IdPortafolio" AND POR."IdCliente" = CAR."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MT ON MT."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = POR."CodigoPortafolio" AND POR."IdMatrizAgrupadorAPV" = 0
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MT."IdProductoAgrupador"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SUB."IdInstrumento"
WHERE CAR."IdEmpresa" = :p_IdEmpresa
AND CAR."IdEstado" = 'VIG'
AND	CAR."IdClienteRelacionado" = case when :p_IdCliente = 0 then :v_IdClienteCuentaPropia else -1 END--solo cuando combobox Cliente vacio
AND	CAR."IdPortafolioRelacionado" = case when :p_IdPortafolio = 0 then CAR."IdPortafolioRelacionado" else -1 END--solo cuando combobox Cliente vacio
<<FECHACONSULTA.CAR.p_TipoFechaConsulta>>
<<CASE.SUB.p_IdClasificacionTipoInstrumento>>
<<CASE.SUB.p_IdTipoInstrumento>>
<<CASE.SUB.p_IdInstrumento>>
<<CASE.SUB.p_Nemotecnico>>
AND CAR."IdTipoInstrumento" = 'PAC'
AND CAR."IdTipoOperacion"   = 'COMPAC'
AND SUB."IdEstado"='VIG'


