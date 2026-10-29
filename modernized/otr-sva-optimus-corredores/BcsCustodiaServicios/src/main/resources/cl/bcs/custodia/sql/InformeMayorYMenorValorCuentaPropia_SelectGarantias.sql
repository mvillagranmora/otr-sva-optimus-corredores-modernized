SELECT 	"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL AS "IdValorizacion" 	
		,GAR."IdEmpresa"
		,GAR."IdCliente" 
		,GAR."IdPortafolio"
		,'GARANTIA' 													AS "TablaOrigen"
		,GAR."IdGarantia"												AS "IdCartera"
		,GAR."FolioGarantia" 											AS "FolioCartera"
		,GAR."IdClasificacionTipoInstrumento"
		,GAR."IdTipoInstrumento"
		,GAR."IdInstrumento"
		,TO_VARCHAR(GAR."FechaVencimientoInstrumento", 'YYYY-MM-DD') 	AS "FechaVencimientoInstrumento"
		,TO_VARCHAR(GAR."FechaVencimientoOperacion", 'YYYY-MM-DD') 	 	AS "FechaVencimientoOperacion"	
		,GAR."EGRIdOperacion" 											AS "IdOperacion"
		,TO_VARCHAR(INS."FechaEmision", 'YYYY-MM-DD')					AS "FechaEmision"
		,TO_VARCHAR(GAR."FechaEmisionInstrumento", 'YYYY-MM-DD')		AS "FechaEmisionInstrumento"
		,TO_VARCHAR(GAR."FechaOperacion", 'YYYY-MM-DD') 				AS "FechaOperacion"
		,TO_VARCHAR(GAR."INGFechaLiquidacion", 'YYYY-MM-DD') 			AS "FechaLiquidacion"
		,GAR."IdMonedaInstrumento"
		,GAR."IdTipoReajuste"
		,GAR."IdMonedaEmision"
		,GAR."IdCondicionLiquidacion"
		,CAR."IdTipoOperacion"
		,GAR."IdEmisor"
		,GAR."EsFungible"
		,GAR."CodigoIFRS"
		,GAR."Nemotecnico"
		,CASE WHEN GAR."IdClasificacionTipoInstrumento" = 'IF' 
			  THEN GAR."NemoSVS" ELSE GAR."Nemotecnico" END 			AS "NemoSVS"
		,'CG'	 														AS "Diferenciador"	
		,CASE WHEN GAR."IdClasificacionTipoInstrumento" = 'IF' 
		  	THEN GAR."INGValorRescate"
		  	ELSE GAR."INGCantidad"
		 END															AS "Cantidad"
		,GAR."MontoGarantia"						   	 	            AS "MontoCompra"
		,GAR."INGMontoATasaCompraActual"								AS "MontoCompraMonedaSistema"
		,GAR."INGMontoCompraActualMonedaInstrumento"					AS "MontoCompraMonedaInstrumento"
		,GAR."INGValorRescate"											AS "ValorRescate" 
		,GAR."PrecioCompraOperacionOrigen" 								AS "PrecioCompra"	
		,GAR."TasaCompraOperacionOrigen"								AS "TasaCompra"
		,CAR."IdBoveda"													AS "IdBoveda"
		,GAR."INGMontoCompraActualMonedaAdicional" AS "MontoMonedaAdicionalInicial"
FROM "BSCL.Custodia.BD::GTA.Garantia" AS GAR
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
	ON CAR."IdCartera" = GAR."IdCarteraOrigen"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS 
	ON INS."IdInstrumento" = GAR."IdInstrumento"
WHERE GAR."IdEstado" = 'VIG'
AND GAR."IdClasificacionTipoInstrumento" IN ('RF','IF')
AND GAR."IdTipoInstrumento" NOT IN ('SIM')
AND GAR."IdCliente" = :IdCliente
AND (:IdPortafolio = 0 OR GAR."IdPortafolio" = :IdPortafolio)	
AND GAR."FechaOperacion" <= :FechaConsulta
AND GAR."IdEmpresa" = :IdEmpresa;