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
   , "EsCustodiaExterna"
FROM
(
SELECT
				sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
			   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
			   ,sby."IdInstrumento" as "IdInstrumento"
			   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
			   ,sby."IdEmisor" as "IdEmisor"
			   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."EsFungible" else 'N' end as "EsFungible"
			   ,EMP."IdCliente"
			   ,EMP."IdPortafolio"
			   ,'S' AS "EsCustodiaExterna"
			   ,COALESCE(MADOR."NombreProductoAgrupador", 'Otros (No APV)') AS "Producto"
			   ,CASE
			   		WHEN :p_AgrupaPorBoveda='N' THEN :v_IdBovedaDefecto
			   		WHEN ifnull(EMP."IdBoveda",'') <> '' THEN EMP."IdBoveda"
			   		WHEN ifnull(orig."IdBoveda",'') <> '' THEN orig."IdBoveda"
			   		ELSE :v_IdBovedaDefecto
			   	END AS "IdBoveda"
			   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN  0 ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio",0) END AS "IdCuentaCustodioPortafolio"
			   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN '' ELSE COALESCE(CCPI."Codigo",'') END AS "NumeroCuentaBoveda"			   
			   ,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF'  then EMP."INGValorRescate" else EMP."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
			   ,COALESCE(sby."INGPrecioSubyacente",0) as "PrecioCompra"						
			   ,COALESCE(sby."INGMontoATasaSubyacenteActual",0) as "MontoCompra" 			
			   ,COALESCE(sby."INGMontoSubyacenteActualMonedaInstrumento",0) as "ValorCompra"	
			   ,COALESCE(sby."INGPrecioMercadoActual",0) as "INGPrecioMercadoActual" 			
			   ,COALESCE(sby."INGTasaMercadoActual",0)as "INGTasaMercadoActual" 				
			   ,COALESCE(sby."INGMontoMercadoActualMonedaInstrumento",0) as "ValorMercado"
			   ,COALESCE(sby."INGMontoATasaMercadoActual",0) as "MontoMercado"
			   ,CASE
			   		WHEN ifnull(sby."IdMonedaInstrumento",'') <> '' THEN sby."IdMonedaInstrumento"
			   		WHEN ifnull(sby."IdTipoReajusteSubyacente",'') <> '' THEN sby."IdTipoReajusteSubyacente"
			   		ELSE ifnull(sby."IdMonedaSistema",'')
			   	END AS "IdMonedaInstrReajuste"
			   ,sby."IdMonedaEmision"
			   ,sby."IdMonedaSistema" AS "IdMonedaPrincipal"
			   ,sby."INGMontoInteresCorridoSubyacenteActual" as "InteresCorrido"
			   ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
               ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
               ,CLI."IdPersona"
               ,inst3."RazonSocial" AS "RazonSocialInstrumento"
			FROM "BSCL.Custodia.BD::HIS.Cartera" AS EMP
			INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (EMP."FolioCartera" = ultSub."FolioCarteraRelacionada")
			INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCartera" as ultPrt on (EMP."IdEmpresa" = ultPrt."IdEmpresa" and EMP."FolioCarteraRelacionada" = ultPrt."FolioCartera")
			INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as prt on (prt."IdCartera" = ultPrt."IdCartera")
			LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on sby."IdSubyacente" = ultSub."IdSubyacente"
			LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" as orig on (orig."IdCartera" = sby."IdCarteraRetirada")
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =:p_IdEmpresa AND CLI."IdCliente" = EMP."IdCliente")
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = EMP."IdPortafolio" AND Portafolio."IdCliente" = EMP."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = EMP."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
			LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = sby."IdInstrumento"
			LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TPO_CC ON TPO_CC."CodigoTipoIndicador" = 'CC'
			LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR_CC ON ATR_CC."IdTipoIndicador" = TPO_CC."IdTipoIndicador" AND ATR_CC."IdTipoEntidad" = 'CLIENTE' AND ATR_CC."IdEntidad" = TO_VARCHAR(EMP."IdCliente")
			WHERE
				EMP."IdEmpresa" =:p_IdEmpresa
			AND	EMP."FechaRespaldo" = :v_FechaConsulta
			AND EMP."IdEstado" = 'VIG'

			<<FECHACONSULTA.EMP.p_TipoFechaConsulta>>
			<<CASE.EMP.p_IdCliente>>
			<<CASE.EMP.p_IdPortafolio>>
			<<CASE.sby.p_IdClasificacionTipoInstrumento>>
			<<CASE.sby.p_IdTipoInstrumento>> 
			<<CASE.sby.p_IdInstrumento>>
			<<CASE.sby.p_Nemotecnico>>
			
			AND EMP."IdTipoInstrumento" IN ('EMP','ERF')
			AND IFNULL(ATR_CC."ValorIndicador",'N') = 'N'
			<<COALESCE.MatrizAgrupador.p_IdProductoAgrupador>>
			
	)
