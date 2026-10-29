SELECT 	"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL AS "IdValorizacion"
		,CAR."IdEmpresa"
		,CAR."IdCliente" 
		,CAR."IdPortafolio"
		,'CARTERA'																									AS "TablaOrigen"
		,CAR."IdCartera"
		,CAR."FolioCartera"
		,COALESCE(SUB."IdClasificacionTipoInstrumento", CAR."IdClasificacionTipoInstrumento")						AS "IdClasificacionTipoInstrumento"
		,COALESCE(SUB."IdTipoInstrumento", CAR."IdTipoInstrumento") 												AS "IdTipoInstrumento"
		,COALESCE(SUB."IdInstrumento",CAR."IdInstrumento")															AS "IdInstrumento"
		,TO_VARCHAR(COALESCE(SUB."FechaVencimientoInstrumento",CAR."FechaVencimientoInstrumento"), 'YYYY-MM-DD')	AS "FechaVencimientoInstrumento"		
		,TO_VARCHAR(CAR."FechaVencimientoOperacion", 'YYYY-MM-DD') 													AS "FechaVencimientoOperacion"
		,CAR."IdCartera"																							AS "IdOperacion"
		,TO_VARCHAR(INS."FechaEmision", 'YYYY-MM-DD')																AS "FechaEmision"
		,TO_VARCHAR(COALESCE(SUB."FechaEmisionInstrumento",CAR."FechaEmisionInstrumento"), 'YYYY-MM-DD')			AS "FechaEmisionInstrumento"		
		,TO_VARCHAR(CAR."FechaOperacion", 'YYYY-MM-DD')															    AS "FechaOperacion"
		,TO_VARCHAR(CAR."INGFechaLiquidacion", 'YYYY-MM-DD')														AS "FechaLiquidacion"
		,COALESCE(SUB."IdMonedaInstrumento", CAR."IdMonedaInstrumento")												AS "IdMonedaInstrumento"
		,COALESCE(SUB."IdTipoReajuste", CAR."IdTipoReajuste")														AS "IdTipoReajuste"		
		,CAR."IdMonedaEmision"
		,CAR."IdCondicionLiquidacion"
		,CAR."IdTipoOperacion"
		,COALESCE(SUB."IdEmisor", CAR."IdEmisor") 																	AS "IdEmisor"		
		,CAR."EsFungible"
		,CAR."CodigoIFRS"
		,SUB."Nemotecnico"    																						AS "Nemotecnico"
		,CASE WHEN COALESCE(SUB."IdClasificacionTipoInstrumento", CAR."IdClasificacionTipoInstrumento") = 'IF' 
			  THEN COALESCE(SUB."NemoSVS", CAR."NemoSVS") ELSE COALESCE(SUB."NemoSVS", CAR."NemoSVS")  END 			AS "NemoSVS"
		,CASE WHEN CAR."IdTipoInstrumento" = 'PRF' 
			  THEN 'PR' ELSE 'EM'																	   END 			AS "Diferenciador"
		,CASE WHEN COALESCE(SUB."IdClasificacionTipoInstrumento", CAR."IdClasificacionTipoInstrumento") = 'IF' 
			  THEN COALESCE(SUB."INGValorRescate", CAR."INGValorRescate")
			  ELSE COALESCE(SUB."INGCantidad",CAR."INGCantidad")
		 END																										AS "Cantidad"
		,CAR."INGMontoATasaCompraActual"																AS "MontoCompra" 
		,COALESCE(CAR."INGMontoATasaCompraActual",SUB."INGMontoATasaCompraActual")									AS "MontoCompraMonedaSistema"
		,CAR."INGMontoCompraActualMonedaInstrumento"																AS "MontoCompraMonedaInstrumento"
		,COALESCE(CAR."INGValorRescate", SUB."INGValorRescate")														AS "ValorRescate"
		,COALESCE(CAR."INGPrecioCompra",0) 																			AS "PrecioCompra"		
		,COALESCE(CAR."INGTasaCompra",0)																			AS "TasaCompra"
		,CAR."IdBoveda"	                                                                                            
		,CAR."INGMontoCompraActualMonedaAdicional" AS "MontoMonedaAdicionalInicial"																							
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" AS SUB 
	ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera" AND SUB."IdEstado" = 'VIG'
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS 
	ON INS."IdInstrumento" = CAR."IdInstrumento"
WHERE CAR."IdEstado" = 'VIG'
AND CAR."IdClasificacionTipoInstrumento" IN ('RF','IF')
AND CAR."IdTipoInstrumento" IN ('PRF', 'ERF')
AND CAR."IdCliente" = :IdCliente
AND (:IdPortafolio = 0 OR CAR."IdPortafolio" = :IdPortafolio)	
AND CAR."FechaOperacion" <= :FechaConsulta 
AND CAR."IdEmpresa" = :IdEmpresa;
