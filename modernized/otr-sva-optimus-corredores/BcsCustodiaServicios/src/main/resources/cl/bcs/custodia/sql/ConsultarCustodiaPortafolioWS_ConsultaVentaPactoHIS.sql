-- Optimizacion: se reemplaza el INNER JOIN a la vista "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarteraSubyacenteBasica"
-- (~200 columnas, joins adicionales, ORDER BY en la propia definicion de la vista) por una subconsulta acotada a
-- las columnas realmente consumidas por este servicio, sin ORDER BY y sin los joins que no aportan a esas columnas
-- (INS.TipoInstrumento, INS.TipoInstrumentoClasificacionRiesgo, INS.InstrumentoValorClasificacionRiesgo).
-- Se corrige ademas el join a PLA.Configuracion: la vista lo hace contra origen."IdEmpresa" (NULL cuando el
-- subyacente no tiene cartera de origen), aqui se usa :p_IdEmpresa directo.
WITH SUB_BASICA AS (
    SELECT
         suby."FechaRespaldo"
        ,suby."FolioCarteraRelacionada"
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
    FROM "BSCL.Custodia.BD::HIS.Subyacente" AS suby
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Inst ON suby."IdInstrumento" = Inst."IdInstrumento"
    LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" AS origen ON (origen."IdCartera" = suby."IdCarteraRetirada" AND suby."FechaRespaldo" = origen."FechaRespaldo")
    LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS conf ON (conf."IdEmpresa" = :p_IdEmpresa AND conf."CodigoParametro" = 'PLA.MonedaSistema')
)
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
   ,"EsPagaderoEn"
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
   ,"MontoCompraInicial"
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
   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN :v_IdBovedaDefecto ELSE RES."IdBoveda" END AS "IdBoveda"
   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN  0 ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio",0) END AS "IdCuentaCustodioPortafolio"
   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN '' ELSE COALESCE(CCPI."Codigo",'') END AS "NumeroCuentaBoveda"			   
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
   ,CASE 
        	WHEN IFNULL(PAC."IdMonedaInstrumento",'') = 'UF'
        		THEN 'CLP'
        	ELSE 
        		PAC."IdMonedaInstrumento"
        END          AS "EsPagaderoEn"   -- Moneda de Pago  
   ,COALESCE(sby."INGMontoInteresCorridoSubyacenteActual",0) AS "InteresCorrido"
   ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
   ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
   ,CLI."IdPersona"
   ,COALESCE(sby."INGMontoATasaSubyacenteActual",0) AS "MontoCompraInicial"
   ,inst3."RazonSocial" AS "RazonSocialInstrumento"
FROM "BSCL.Custodia.BD::HIS.Cartera" AS PAC
INNER JOIN SUB_BASICA as sby on (PAC."FolioCartera" = sby."FolioCarteraRelacionada" AND  sby."FechaRespaldo" = PAC."FechaRespaldo")
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= sby."FolioSubyacente"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = :p_IdEmpresa AND CLI."IdCliente" = CASE WHEN :p_IdCliente > 0 AND :p_IdCliente = :v_IdClienteCuentaPropia THEN PAC."IdClienteRelacionado" ELSE PAC."IdCliente" END)
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PAC."IdPortafolio" AND Portafolio."IdCliente" = PAC."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PAC."IdCuentaCustodioPortafolio"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = sby."IdInstrumento"
WHERE 
	PAC."IdEmpresa" = :p_IdEmpresa
AND	PAC."FechaRespaldo" = :v_FechaConsulta
AND PAC."IdEstado" = 'VIG'
AND	CASE WHEN :p_IdCliente > 0 AND :p_IdCliente = :v_IdClienteCuentaPropia THEN PAC."IdClienteRelacionado" ELSE PAC."IdCliente" END = case when :p_IdCliente > 0 then :p_IdCliente else PAC."IdCliente" END
AND	CASE WHEN :p_IdCliente > 0 AND :p_IdPortafolio = :v_IdPortafolioCuentaPropia THEN PAC."IdPortafolioRelacionado" ELSE PAC."IdPortafolio" END = case when :p_IdPortafolio > 0 then :p_IdPortafolio else PAC."IdPortafolio" END
<<FECHACONSULTA.PAC.p_TipoFechaConsulta>>
<<CASE.sby.p_IdClasificacionTipoInstrumento>>
<<CASE.sby.p_IdTipoInstrumento>>
<<CASE.sby.p_IdInstrumento>>
<<CASE.sby.p_Nemotecnico>>
AND PAC."IdTipoInstrumento" = 'PAC'
AND PAC."IdTipoOperacion" = CASE WHEN :p_IdCliente = :v_IdClienteCuentaPropia THEN 'VENPAC' ELSE 'COMPAC' END
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
   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN :v_IdBovedaDefecto ELSE RES."IdBoveda" END AS "IdBoveda"
   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN  0 ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio",0) END AS "IdCuentaCustodioPortafolio"
   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN '' ELSE COALESCE(CCPI."Codigo",'') END AS "NumeroCuentaBoveda"			   
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
        	WHEN IFNULL(PAC."IdMonedaInstrumento",'') = 'UF'
        		THEN 'CLP'
        	ELSE 
        		PAC."IdMonedaInstrumento"
        END          AS "EsPagaderoEn"
   ,CASE
   		WHEN IFNULL(sby."IdMonedaEmision",'') <> '' THEN sby."IdMonedaEmision"
   		WHEN IFNULL(sby."IdTipoReajusteSubyacente",'') <> '' THEN sby."IdTipoReajusteSubyacente"
   		ELSE sby."IdMonedaInstrumento" 
	END AS "IdMonedaEmision"
   ,sby."IdMonedaSistema" AS "IdMonedaPrincipal"
   ,COALESCE(sby."INGMontoInteresCorridoSubyacenteActual",0) AS "InteresCorrido"
   ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
   ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
   ,CLI."IdPersona"
   ,COALESCE(sby."INGMontoATasaSubyacenteActual",0) AS "MontoCompraInicial"
   ,inst3."RazonSocial" AS "RazonSocialInstrumento"
FROM "BSCL.Custodia.BD::HIS.Cartera" AS PAC
INNER JOIN SUB_BASICA as sby on (PAC."FolioCartera" = sby."FolioCarteraRelacionada" AND  sby."FechaRespaldo" = PAC."FechaRespaldo")
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= sby."FolioSubyacente"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = :p_IdEmpresa AND CLI."IdCliente" = CASE WHEN :p_IdCliente > 0 AND :p_IdCliente = :v_IdClienteCuentaPropia THEN PAC."IdClienteRelacionado" ELSE PAC."IdCliente" END)
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PAC."IdPortafolio" AND Portafolio."IdCliente" = PAC."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PAC."IdCuentaCustodioPortafolio"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = sby."IdInstrumento"
WHERE 
	PAC."IdEmpresa" = :p_IdEmpresa
AND	PAC."FechaRespaldo" = :v_FechaConsulta
AND PAC."IdEstado" = 'VIG'
AND	PAC."IdClienteRelacionado" = case when :p_IdCliente = 0 then :v_IdClienteCuentaPropia else -1 END--solo cuando combobox Cliente vacio
AND	PAC."IdPortafolioRelacionado" = case when :p_IdPortafolio = 0 then PAC."IdPortafolioRelacionado" else -1 END--solo cuando combobox Cliente vacio
<<FECHACONSULTA.PAC.p_TipoFechaConsulta>>
<<CASE.sby.p_IdClasificacionTipoInstrumento>>
<<CASE.sby.p_IdTipoInstrumento>>
<<CASE.sby.p_IdInstrumento>>
<<CASE.sby.p_Nemotecnico>>
AND PAC."IdTipoInstrumento" = 'PAC'
AND PAC."IdTipoOperacion" = 'VENPAC'
)
