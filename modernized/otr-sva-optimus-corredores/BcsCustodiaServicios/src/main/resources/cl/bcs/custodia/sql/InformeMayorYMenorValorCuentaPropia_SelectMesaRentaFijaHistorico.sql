WITH Operaciones AS (
	SELECT 
			OPE."IdEmpresa"
			,CASE 
				WHEN :IdClienteCorredor = :IdCliente THEN 
					OPE."IdCliente" 
				ELSE 
					MES."IdCliente" 
			 END AS "IdCliente" 
			,CASE 
				WHEN :IdClienteCorredor = :IdCliente THEN 
					OPE."IdPortafolio" 
				ELSE OPE."IdPortafolioCorredor" 
			 END AS "IdPortafolio"
			,'CARTERA' 														AS "TablaOrigen"
			,RES."IdCarteraOrigen" AS "IdCartera"
			,CASE 
				WHEN OPE."AfectaCustodia" = 'N' THEN RES."FolioCarteraCustodia"
				ELSE RES."FolioCarteraOrigen" 
			END AS "FolioCartera"
			,OPE."FolioOperacion"
			,SUB."IdClasificacionTipoInstrumento"
			,SUB."IdTipoInstrumento"
			,SUB."IdInstrumento"
			,SUB."FechaVencimientoInstrumento"
			,TO_VARCHAR(OPE."FechaVencimiento",'YYYY-MM-DD')                AS "FechaVencimientoOperacion"
			,RES."IdCarteraOrigen"											AS "IdOperacion"
			,INS."FechaEmision"
			,TO_VARCHAR(SUB."FechaEmisionInstrumento",'YYYY-MM-DD')         AS "FechaEmisionInstrumento"
			,TO_VARCHAR(SUB."INGFechaOperacion",'YYYY-MM-DD')               AS "FechaOperacion"
			,TO_VARCHAR(OPE."FechaCondicionLiquidacion",'YYYY-MM-DD') 				AS "FechaLiquidacion"
			,SUB."IdMonedaInstrumento"										
			,SUB."IdTipoReajuste"
			,SUB."IdMonedaEmision"
			,OPE."IdCondicionLiquidacion"
			,OPE."IdTipoOperacion"
			,SUB."IdEmisor"
			,SUB."EsFungible"
			,RES."CodigoIFRS"
			,SUB."Nemotecnico"
			,SUB."NemoSVS" 									   				
			,'PD'                           								AS "Diferenciador"
			,CASE WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' 
				  THEN SUB."INGValorRescate" 
				  ELSE SUB."INGCantidad" 
			 END 															AS "Cantidad"
			,RES."MontoValorizado"					                        AS "MontoCompra"
			,SUB."INGMontoATasaCompraActual"								AS "MontoCompraMonedaSistema"
			,SUB."INGMontoCompraActualMonedaInstrumento"					AS "MontoCompraMonedaInstrumento"
			,SUB."INGValorRescate"											AS "ValorRescate"
			,SUB."INGPrecioSubyacente"	                                    AS "PrecioCompra" 
			,CASE
				WHEN COALESCE(PARAM."Valor", 'N') = 'S' THEN 
					COALESCE(RES."Tasa", CAR."INGTasaCompra")
				ELSE 
					SUB."TasaCompraOperacionRelacionada"
			 END AS "TasaCompra"
			,SUB."INGMontoCompraActualMonedaAdicional" AS "MontoMonedaAdicionalInicial"
			,RES."IdBoveda"													AS "IdBoveda"
			,RES."IdConcepto" 
			,RES."IdBolsa" 
			,OPE."IdTipoProducto"
	FROM "BSCL.Mesa.BD::MES.Operacion" OPE 
	INNER JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON OPE."IdMesaOrigen" = MES."IdMesa"
	INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."IdOperacion" = OPE."IdOperacion"
	INNER JOIN "BSCL.Custodia.BD::HIS.Subyacente" AS SUB ON SUB."FolioSubyacente" = RES."FolioSubyacente" AND SUB."FechaRespaldo" = :FechaConsulta
	LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" AS CAR ON RES."IdCarteraOrigen"  = CAR."IdCartera" AND CAR."FechaRespaldo" = :FechaConsulta
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = SUB."IdInstrumento"
	LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" PARAM ON PARAM."CodigoParametro" = 'TASARECOMPRAVENTACONPACTO'
	WHERE (MES."IdCliente" = :IdCliente OR OPE."IdCliente" = :IdCliente) 
	AND MES."IdPortafolio" = CASE WHEN :IdPortafolio = 0 THEN MES."IdPortafolio" ELSE :IdPortafolio END	
	AND SUB."IdEstado" = 'VIG'
	AND OPE."IdTipoOperacion"='COMPAC'
	AND COALESCE(CAR."TipoCarteraCorredor", '') <> 'CC'
	AND SUB."FechaRespaldo" = :FechaConsulta
	AND OPE."FechaOperacion" <= :FechaConsulta
	AND OPE."IdEmpresa" = :IdEmpresa
	UNION ALL
	SELECT 	 
	     	 OPE."IdEmpresa"
			,CASE 
				WHEN :IdClienteCorredor = :IdCliente THEN 
					OPE."IdCliente" 
				ELSE 
					MES."IdCliente" 
			 END AS "IdCliente" 
			,CASE 
				WHEN :IdClienteCorredor = :IdCliente THEN 
					OPE."IdPortafolio" 
				ELSE OPE."IdPortafolioCorredor" 
			 END AS "IdPortafolio" 
			,'CARTERA'														AS "TablaOrigen"
			,RES."IdCarteraOrigen" AS "IdCartera"
			,CASE 
				WHEN OPE."AfectaCustodia" = 'N' THEN RES."FolioCarteraCustodia"
				ELSE RES."FolioCarteraOrigen" 
			 END AS "FolioCartera"
			,OPE."FolioOperacion"
			,SUB."IdClasificacionTipoInstrumento"
			,SUB."IdTipoInstrumento"
			,SUB."IdInstrumento"
			,SUB."FechaVencimientoInstrumento"
			,TO_VARCHAR(OPE."FechaVencimiento",'YYYY-MM-DD')                AS "FechaVencimientoOperacion"
			,OPE."IdOperacion"
			,INS."FechaEmision"
			,TO_VARCHAR(SUB."FechaEmisionInstrumento",'YYYY-MM-DD')          AS "FechaEmisionInstrumento"
			,SUB."INGFechaOperacion" 										AS "FechaOperacion"
			,OPE."FechaCondicionLiquidacion" 										AS "FechaLiquidacion"
			,SUB."IdMonedaInstrumento"
			,SUB."IdTipoReajuste"											AS "IdTipoReajuste"
			,SUB."IdMonedaEmision"
			,OPE."IdCondicionLiquidacion"
			,OPE."IdTipoOperacion"
			,SUB."IdEmisor"
			,SUB."EsFungible"
			,RES."CodigoIFRS"
			,RES."Nemotecnico"
			,SUB."NemoSVS"
			,'PC'                                                           AS "Diferenciador"
			,CASE WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' 
				  THEN SUB."INGValorRescate"
				  ELSE SUB."INGCantidad"
			 END															AS "Cantidad"
			,RES."MontoValorizado"					                        AS "MontoCompra"
			,SUB."INGMontoATasaCompraActual"								AS "MontoCompraMonedaSistema"
			,SUB."INGMontoCompraActualMonedaInstrumento"					AS "MontoCompraMonedaInstrumento"
			,SUB."INGValorRescate"											AS "ValorRescate"
			,SUB."INGPrecioSubyacente"	                                    AS "PrecioCompra" 
			,CASE
				WHEN COALESCE(PARAM."Valor", 'N') = 'S' THEN 
					COALESCE(RES."Tasa", CAR."INGTasaCompra") 
				ELSE 
					SUB."TasaCompraOperacionRelacionada"
			 END AS "TasaCompra"
			,SUB."INGMontoCompraActualMonedaAdicional" AS "MontoMonedaAdicionalInicial"
			,RES."IdBoveda"													AS "IdBoveda"
			,RES."IdConcepto" 
			,RES."IdBolsa" 
			,OPE."IdTipoProducto"
	FROM "BSCL.Mesa.BD::MES.Operacion" OPE 
	INNER JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON OPE."IdMesaOrigen" = MES."IdMesa"
	INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."IdOperacion" = OPE."IdOperacion"
	INNER JOIN "BSCL.Custodia.BD::HIS.Subyacente" AS SUB ON SUB."FolioSubyacente" = RES."FolioSubyacente" AND SUB."FechaRespaldo" = :FechaConsulta
	LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" AS CAR ON RES."IdCarteraOrigen" = CAR."IdCartera"  AND CAR."FechaRespaldo" = :FechaConsulta
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = SUB."IdInstrumento"
	LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" PARAM ON PARAM."CodigoParametro" = 'TASARECOMPRAVENTACONPACTO'
	WHERE (MES."IdCliente" = :IdCliente OR OPE."IdCliente" = :IdCliente)
		AND SUB."FechaRespaldo" = :FechaConsulta
		AND MES."IdPortafolio" = CASE WHEN :IdPortafolio = 0 THEN MES."IdPortafolio" ELSE :IdPortafolio END	
		AND SUB."IdEstado" = 'VIG'
		AND OPE."IdTipoOperacion"='VENPAC'
		AND COALESCE(CAR."TipoCarteraCorredor", '') <> 'CC'	
		AND OPE."FechaOperacion" <= :FechaConsulta
		AND OPE."IdEmpresa" = :IdEmpresa
	UNION ALL 
	SELECT 	 
			 OPE."IdEmpresa"
			,CASE 
				WHEN :IdClienteCorredor = :IdCliente THEN 
					OPE."IdCliente" 
				ELSE 
					MES."IdCliente" 
			 END AS "IdCliente" 
			,CASE 
				WHEN :IdClienteCorredor = :IdCliente THEN 
					OPE."IdPortafolio" 
				ELSE OPE."IdPortafolioCorredor" 
			 END AS "IdPortafolio"
			,'CARTERA'														AS "TablaOrigen"
			,RES."IdCarteraOrigen" AS "IdCartera"
			,CASE 
				WHEN OPE."AfectaCustodia" = 'N' THEN RES."FolioCarteraCustodia"
				ELSE RES."FolioCarteraOrigen" 
			 END AS "FolioCartera"
			,OPE."FolioOperacion"
			,SUB."IdClasificacionTipoInstrumento"
			,SUB."IdTipoInstrumento"
			,SUB."IdInstrumento"
			,SUB."FechaVencimientoInstrumento"
			,TO_VARCHAR(OPE."FechaVencimiento",'YYYY-MM-DD')                AS "FechaVencimientoOperacion"
			,OPE."IdOperacion"
			,INS."FechaEmision"
			,SUB."FechaEmisionInstrumento"
			,SUB."INGFechaOperacion" 										AS "FechaOperacion"
			,OPE."FechaCondicionLiquidacion" 										AS "FechaLiquidacion"
			,SUB."IdMonedaInstrumento"
			,SUB."IdTipoReajuste"											AS "IdTipoReajuste"
			,SUB."IdMonedaEmision"
			,OPE."IdCondicionLiquidacion"
			,OPE."IdTipoOperacion"
			,SUB."IdEmisor"
			,SUB."EsFungible"
			,RES."CodigoIFRS"
			,RES."Nemotecnico"
			,SUB."NemoSVS"
			,'PP'                                                           AS "Diferenciador"
			,CASE WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' 
				  THEN SUB."INGValorRescate"
				  ELSE SUB."INGCantidad"
			 END															AS "Cantidad"
			,RES."MontoValorizado"					                        AS "MontoCompra"
			,SUB."INGMontoATasaCompraActual"								AS "MontoCompraMonedaSistema"
			,SUB."INGMontoCompraActualMonedaInstrumento"					AS "MontoCompraMonedaInstrumento"
			,SUB."INGValorRescate"											AS "ValorRescate"
			,SUB."INGPrecioSubyacente"	                                    AS "PrecioCompra" 
			,CASE
				WHEN COALESCE(PARAM."Valor", 'N') = 'S' THEN 
					COALESCE(RES."Tasa", CAR."INGTasaCompra") 
				ELSE 
					SUB."TasaCompraOperacionRelacionada"
			 END AS "TasaCompra"
			,SUB."INGMontoCompraActualMonedaAdicional" AS "MontoMonedaAdicionalInicial"
			,RES."IdBoveda"													AS "IdBoveda"
			,RES."IdConcepto" 
			,RES."IdBolsa" 
			,OPE."IdTipoProducto"
	FROM 	"BSCL.Mesa.BD::MES.Operacion" OPE 
	INNER JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON OPE."IdMesaOrigen" = MES."IdMesa"
	INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."IdOperacion" = OPE."IdOperacion"
	INNER JOIN "BSCL.Custodia.BD::HIS.Subyacente" AS SUB ON SUB."FolioSubyacente" = RES."FolioSubyacente" AND SUB."FechaRespaldo" = :FechaConsulta
	LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" AS CAR ON RES."IdCarteraOrigen" = CAR."IdCartera" AND CAR."FechaRespaldo" = :FechaConsulta
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = SUB."IdInstrumento"
	LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" PARAM ON PARAM."CodigoParametro" = 'TASARECOMPRAVENTACONPACTO'
	WHERE 	(MES."IdCliente" = :IdCliente OR OPE."IdCliente" = :IdCliente)  
		AND MES."IdPortafolio" = CASE WHEN :IdPortafolio = 0 THEN MES."IdPortafolio" ELSE :IdPortafolio END	
		AND SUB."IdEstado" = 'VIG'
		AND OPE."IdTipoOperacion"='VENPAC'
		AND CAR."TipoCarteraCorredor" = 'CC'
		AND SUB."FechaRespaldo" = :FechaConsulta
		AND OPE."FechaOperacion" <= :FechaConsulta
		AND OPE."IdEmpresa" = :IdEmpresa
	) 
SELECT 
		"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL AS "IdValorizacion"
		,"IdEmpresa"
		,"IdCliente" 
		,"IdPortafolio"
		,"TablaOrigen"
		,"IdCartera"
		,"FolioCartera"
		,"FolioOperacion"
		,"IdClasificacionTipoInstrumento"
		,"IdTipoInstrumento"
		,"IdInstrumento"
		,TO_VARCHAR("FechaVencimientoInstrumento", 'YYYY-MM-DD') AS "FechaVencimientoInstrumento"
		,TO_VARCHAR("FechaVencimientoOperacion", 'YYYY-MM-DD') AS "FechaVencimientoOperacion"
		,"IdOperacion"
		,TO_VARCHAR("FechaEmision", 'YYYY-MM-DD') AS "FechaEmision"
		,TO_VARCHAR("FechaEmisionInstrumento", 'YYYY-MM-DD') AS "FechaEmisionInstrumento"
		,TO_VARCHAR("FechaOperacion", 'YYYY-MM-DD') AS "FechaOperacion"
		,TO_VARCHAR("FechaLiquidacion", 'YYYY-MM-DD') AS "FechaLiquidacion"
		,"IdMonedaInstrumento"		
		,"IdTipoReajuste"
		,"IdMonedaEmision"
		,"IdCondicionLiquidacion"
		,"IdTipoOperacion"
		,"IdEmisor"		
		,"EsFungible"
		,"CodigoIFRS"
		,"Nemotecnico"
		,"NemoSVS" 									   				
		,"Diferenciador"
		,"Cantidad"
		,"MontoCompra"
		,"MontoCompraMonedaSistema"
		,"MontoCompraMonedaInstrumento"
		,"ValorRescate"
		,"PrecioCompra"
		,"TasaCompra"
		,"IdBoveda"
		,"MontoMonedaAdicionalInicial"
FROM Operaciones;
