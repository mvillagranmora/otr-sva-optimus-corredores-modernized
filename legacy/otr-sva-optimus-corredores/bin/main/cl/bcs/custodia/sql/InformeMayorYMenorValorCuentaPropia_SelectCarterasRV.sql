SELECT 	 
		"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL AS "IdValorizacion"
		,CAR."IdEmpresa"
		,CAR."IdCliente" 
		,CAR."IdPortafolio"
		,'CARTERA' 																											AS "TablaOrigen"
		,CAR."IdCartera"
		,CAR."FolioCartera"
		,COALESCE(SUB."IdClasificacionTipoInstrumento", CAR."IdClasificacionTipoInstrumento")								AS "IdClasificacionTipoInstrumento"
		,COALESCE(SUB."IdTipoInstrumento", CAR."IdTipoInstrumento") 														AS "IdTipoInstrumento"
		,COALESCE(SUB."IdInstrumento",CAR."IdInstrumento")																	AS "IdInstrumento"
		,TO_VARCHAR(COALESCE(SUB."FechaVencimientoInstrumento", CAR."FechaVencimientoInstrumento"), 'YYYY-MM-DD') 			AS "FechaVencimientoInstrumento"
		,TO_VARCHAR(COALESCE(SUB."FechaVencimientoOperacion", CAR."FechaVencimientoOperacion") , 'YYYY-MM-DD') 				AS "FechaVencimientoOperacion"
		,CAR."FolioCartera"																									AS "IdOperacion"
		,TO_VARCHAR(INS."FechaEmision", 'YYYY-MM-DD')																		AS "FechaEmision"
		,TO_VARCHAR(COALESCE(SUB."FechaEmisionInstrumento",CAR."FechaEmisionInstrumento"), 'YYYY-MM-DD') 					AS "FechaEmisionInstrumento"
		,TO_VARCHAR(CAR."FechaOperacion",'YYYY-MM-DD') 																	    AS "FechaOperacion"
		,TO_VARCHAR(CAR."INGFechaLiquidacion", 'YYYY-MM-DD') 																AS "FechaLiquidacion"
		,COALESCE(SUB."IdMonedaInstrumento", CAR."IdMonedaInstrumento")														AS "IdMonedaInstrumento"
		,CAR."IdTipoReajuste"
		,CAR."IdMonedaEmision"
		,CAR."IdCondicionLiquidacion"
		,CAR."IdTipoOperacion"
		,COALESCE(SUB."IdEmisor", CAR."IdEmisor") 																			AS "IdEmisor"
		,CAR."EsFungible"
		,CAR."CodigoIFRS"
		,CAR."Nemotecnico" 																									AS "Nemotecnico"
		,COALESCE(SUB."NemoSVS", CAR."NemoSVS") 																			AS "NemoSVS"		
		,CASE 
			WHEN CAR."IdTipoInstrumento" = 'SIM' THEN 'SIM'
			ELSE 'CD'
		END AS "Diferenciador"
		,COALESCE(SUB."INGCantidad",CAR."INGCantidad")																		AS "Cantidad"
		,CAR."INGMontoATasaCompraActual"																		AS "MontoCompra"
		,COALESCE(SUB."INGMontoATasaCompraActual", CAR."INGMontoATasaCompraActual")											AS "MontoCompraMonedaSistema"
		,CAR."INGMontoCompraActualMonedaInstrumento"																		AS "MontoCompraMonedaInstrumento"
		,COALESCE(SUB."INGValorRescate", CAR."INGValorRescate")																AS "ValorRescate"
		,COALESCE(CAR."INGPrecioCompra",0) 																					AS "PrecioCompra"		
		,COALESCE(CAR."INGTasaCompra",0)																					AS "TasaCompra"
		,CAR."IdBoveda"																										AS "IdBoveda"
		,CAR."INGMontoCompraActualMonedaAdicional" AS "MontoMonedaAdicionalInicial"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" AS SUB 
	ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera" AND SUB."IdEstado" = 'VIG'
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS 
	ON INS."IdInstrumento" = CAR."IdInstrumento"
WHERE CAR."IdEstado" = 'VIG'
AND CAR."IdClasificacionTipoInstrumento" = 'RV'
AND (CAR."IdTipoInstrumento" NOT IN ('PAC', 'FWD', 'RRV','RRF', 'PRT', 'EMP') OR (CAR."IdTipoInstrumento" = 'SIM' AND CAR."IdTipoOperacion" = 'VENSIM'))
AND CAR."IdCliente" = :IdCliente
AND (:IdPortafolio = 0 OR CAR."IdPortafolio" = :IdPortafolio)	
AND CAR."FechaOperacion" <= :FechaConsulta 
AND CAR."IdEmpresa" = :IdEmpresa;