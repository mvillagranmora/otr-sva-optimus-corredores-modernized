WITH 
	CarteraPrestamo AS (
		SELECT  ROW_NUMBER() OVER(PARTITION BY "FolioCartera" ORDER BY "IdCartera" DESC) AS "ultimaCartera",
				"IdCartera", "FolioCartera", "IdCliente", "IdPortafolio", "IdCuentaCustodioPortafolio",
				"IdTipoInstrumento", "IdTipoPrestamo", "IdEstado", "IdEmpresa", "Cantidad", "INGCantidad",
				"INGFechaLiquidacion", "INGFechaOperacion", "IdBoveda", "IdMonedaSistema", "IdMonedaInstrumento"
		FROM "BSCL.Custodia.BD::CUS.Cartera"
		WHERE "IdTipoInstrumento" IN ('PRT','PRF')	 
		  AND "IdTipoPrestamo" IN ('PRTVTC','PRVCRF')
		  AND "IdCliente"			= CASE WHEN :p_IdCliente > 0 THEN :p_IdCliente ELSE "IdCliente" END			
		  AND "IdPortafolio" 		= CASE WHEN :p_IdPortafolio > 0 THEN :p_IdPortafolio ELSE "IdPortafolio" END
		  AND "IdEstado"        	= 'VIG'
		  AND "IdEmpresa" 			=:p_IdEmpresa
	),
	SubyacentePrestamo AS (
		SELECT  ROW_NUMBER() OVER(PARTITION BY "FolioCartera" ORDER BY "IdSubyacente" DESC) AS "ultimoSubyacente",
				sb.*
		FROM "BSCL.Custodia.BD::CUS.Subyacente" AS sb
		INNER JOIN CarteraPrestamo as car on car."FolioCartera" = sb."FolioCarteraRelacionada"
		WHERE car."ultimaCartera" = 1
	),
    IndicadorContratoCustodia AS (
		SELECT  ATR_CC."IdEntidad" AS "IdCliente",
				ATR_CC."ValorIndicador" AS "ContratoCustodia"
		FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO_CC
		INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR_CC 
				ON ATR_CC."IdTipoIndicador" = TPO_CC."IdTipoIndicador" 
			   AND ATR_CC."IdTipoEntidad" = 'CLIENTE'
		WHERE TPO_CC."CodigoTipoIndicador" = 'CC'
	),
	consultarAsignacionesPrestamo AS (
		SELECT 
			 prt."FolioCartera" as "FolioCarteraPrestamo"
			,AVG(prt."Cantidad") as "CantidadPrestamo"
			,COALESCE(SUM(emp."INGCantidad"),0) as "CantidadAsignada"
			,AVG(prt."INGCantidad") - COALESCE(SUM(emp."INGCantidad"),0) as "CantidadPorAsignar"
		FROM CarteraPrestamo as prt
		LEFT JOIN  "BSCL.Custodia.BD::CUS.Cartera" as emp on 
			prt."IdEmpresa" = emp."IdEmpresa" and
			prt."FolioCartera" = emp."FolioCarteraRelacionada" and
			emp."IdTipoInstrumento" IN ('EMP','ERF') and
			emp."IdEstado" = 'VIG' and
			emp."FolioCarteraRelacionada" > 0
		WHERE prt."ultimaCartera" = 1
		GROUP BY  prt."IdEmpresa", prt."FolioCartera"
	)
SELECT 
		sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
	   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
	   ,sby."IdInstrumento" as "IdInstrumento"
	   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
	   ,sby."IdEmisor" as "IdEmisor"
	   ,case when sby."IdClasificacionTipoInstrumento"= 'IF' then sby."EsFungible" else 'N' end as "EsFungible"
	   ,PRE."IdCliente"
	   ,PRE."IdPortafolio"
	   ,COALESCE(MADOR."NombreProductoAgrupador", 'Otros (No APV)') AS "Producto"
	   ,CASE 
	   		WHEN :p_AgrupaPorBoveda='N' THEN :v_IdBovedaDefecto 
	   		WHEN :p_AgrupaPorBoveda='S' AND IFNULL(PRE."IdBoveda",'')<>'' THEN PRE."IdBoveda" 
	   		ELSE 'DCV' 
	   	END AS "IdBoveda"
	   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN  0 ELSE COALESCE(CCPI."IdCuentaCustodioPortafolio",0) END AS "IdCuentaCustodioPortafolio"
	   ,CASE WHEN :p_AgrupaPorBoveda='N' THEN '' ELSE COALESCE(CCPI."Codigo",'') END AS "NumeroCuentaBoveda"
	   ,PRE."INGCantidad" AS "Cantidad"
	   ,ASIG."CantidadPrestamo"	   
	   ,ASIG."CantidadPorAsignar"
	   ,ASIG."CantidadAsignada"
	   ,COALESCE(sby."INGPrecioSubyacente",0) as "PrecioCompra"
	   ,COALESCE(sby."INGMontoATasaSubyacenteActual",0) as "MontoCompra"			
	   ,COALESCE(sby."INGMontoSubyacenteActualMonedaInstrumento",0) as "ValorCompra" 	
	   ,COALESCE(sby."INGPrecioMercadoActual",0) as "INGPrecioMercadoActual"			
	   ,COALESCE(sby."INGTasaMercadoActual",0) as "INGTasaMercadoActual"				
	   ,COALESCE(sby."INGMontoMercadoActualMonedaInstrumento",0) as "ValorMercado"
	   ,COALESCE(sby."INGMontoATasaMercadoActual",0) as "MontoMercado"
	   ,COALESCE(sby."IdMonedaInstrumento", INS."IdMoneda", sby."IdTipoReajuste", PRE."IdMonedaSistema", conf."Valor", 'CLP') AS "IdMonedaInstrReajuste"
	   ,COALESCE(PRE."IdMonedaSistema", conf."Valor", 'CLP') AS "IdMonedaPrincipal"
	   ,COALESCE(sby."IdMonedaEmision", sby."IdTipoReajuste", sby."IdMonedaInstrumento") AS "IdMonedaEmision"
	   ,sby."INGMontoInteresCorridoSubyacenteActual" as "InteresCorrido"
	   ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
       ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
       ,CLI."IdPersona"
       ,INS."RazonSocial" AS "RazonSocialInstrumento"
       ,pre."IdEstado"
       ,CASE WHEN IFNULL(PRE."IdMonedaInstrumento",'') = 'UF' THEN 'CLP' ELSE PRE."IdMonedaInstrumento" END AS "EsPagaderoEn"   -- Moneda de Pago
FROM CarteraPrestamo AS PRE
INNER JOIN SubyacentePrestamo as sby on (PRE."FolioCartera" = sby."FolioCarteraRelacionada") AND "ultimoSubyacente" = 1
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = sby."IdInstrumento"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = PRE."IdCliente"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PRE."IdPortafolio" AND Portafolio."IdCliente" = PRE."IdCliente"
INNER JOIN consultarAsignacionesPrestamo ASIG ON ASIG."FolioCarteraPrestamo" = PRE."FolioCartera"
LEFT JOIN IndicadorContratoCustodia IND ON IND."IdCliente" = PRE."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PRE."IdCuentaCustodioPortafolio"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS conf ON (conf."IdEmpresa" = PRE."IdEmpresa" AND conf."CodigoParametro" = 'PLA.MonedaSistema')
WHERE	
	PRE."ultimaCartera" 					= 1
AND COALESCE(IND."ContratoCustodia",'N')	= 'N'

<<FECHACONSULTA.PRE.p_TipoFechaConsulta>>
<<CASE.sby.p_IdClasificacionTipoInstrumento>>
<<CASE.sby.p_IdTipoInstrumento>> 
<<CASE.sby.p_IdInstrumento>>
<<CASE.sby.p_Nemotecnico>>

<<COALESCE.MatrizAgrupador.p_IdProductoAgrupador>>


