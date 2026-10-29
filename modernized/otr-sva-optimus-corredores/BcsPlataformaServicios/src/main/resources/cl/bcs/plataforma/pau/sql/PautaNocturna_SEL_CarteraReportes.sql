SELECT	--TOP 1
		"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL AS "IdProcesamientoValorizaCustodiaMasiva",
		dt."IdCartera"		AS "IdTablaOrigen",
		'CARTERA' 			AS "TablaOrigen",
		'PENDIENTE' 		AS "EstadoProcesamiento",
		dt."FechaOperacion" AS "PQ_FechaOperacion",
		'S'					AS "PQ_ValorizaACompra",
		dt."IdClasificacionTipoInstrumento" AS "PQ_IdClasificacionTipoInstrumento",
		dt."IdTipoInstrumento" 				AS "PQ_IdTipoInstrumento",
		dt."IdInstrumento" 					AS "PQ_IdInstrumento",
		dt."Nemotecnico" 					AS "PQ_Nemotecnico",
		CASE WHEN COALESCE( dt."IdMonedaInstrumento", '' ) = '' THEN dt."IdMonedaSistema"
		ELSE dt."IdMonedaInstrumento" END 	AS "PQ_IdMoneda",
		dt."INGCantidad"					AS "PQ_Cantidad",
		dt."INGPrecioCompra" 				AS "PQ_Precio",
		dt."Tamano" 						AS "PQ_Tamano",
		'CARTERA' 							AS "PQ_UsoPrecio",
		NULL								AS "PQ_TipoCartera", --se calcula en valorizarCustodia
		'NORMATIVO' 						AS "PQ_IdTipoCriterioTasa",
		dt."FechaVencimientoOperacion"		AS "PQ_FechaVencimientoOperacion",
		dt."INGTasaCompra"					AS "PQ_Tasa",
		--* Carga no graba este dato
		--* dt."PlazoEnDiasOperacion" 			AS "PQ_Plazo",
		CAST( DAYS_BETWEEN ( dt."FechaOperacion", dt."FechaVencimientoOperacion" ) 
			  AS DECIMAL )					AS "PQ_Plazo", 	
		CAST( DAYS_BETWEEN (	
				TO_SECONDDATE( :FechaValorizacion, 'YYYY-MM-DD' ),
				TO_SECONDDATE( dt."FechaVencimientoOperacion", 'YYYY-MM-DD') 
			  ) AS DECIMAL )				AS "PQ_PlazoResidual", 	
		NULL 								AS "PQ_Base",				-- Consultar donde sacar!
		dt."IdCliente" 						AS "PQ_IdCliente",		
		dt."IdPortafolio"					AS "PQ_IdPortafolio",		
		dt."FechaLiquidacion"				AS "PQ_FechaLiquidacion",
		--* ---------------------------------------------------------------------		
		--* RF/IF
		--* ---------------------------------------------------------------------
		dt."FechaOperacion"					AS "WS_FechaOperacion",
		'S'									AS "WS_ValorizaACompra",
		COALESCE(dt."ValorizaPor",'T') 		AS "WS_ValorizaPor",
		dt."IdClasificacionTipoInstrumento"	AS "WS_IdClasificacionTipoInstrumento",
		dt."IdTipoInstrumento" 				AS "WS_IdTipoInstrumento",
		dt."IdInstrumento" 					AS "WS_IdInstrumento",
		dt."Nemotecnico" 					AS "WS_Nemotecnico",
		dt."IdEmisor" 						AS "WS_IdEmisor",
		dt."FechaEmisionInstrumento"		AS "WS_FechaEmision",
		dt."FechaVencimientoInstrumento" 	AS "WS_FechaVencimiento",
		dt."IdTipoReajuste" 				AS "WS_IdTipoReajuste",
		dt."INGCantidad" 					AS "WS_Cantidad",
		dt."INGValorRescate"				AS "WS_ValorRescate",
		dt."INGPrecioCompra"				AS "WS_PrecioCompra",
		dt."INGTasaCompra"					AS "WS_TasaCompra",
		NULL 								AS "WS_PrecioSubyacente",
		NULL 								AS "WS_TasaSubyacente",
		NULL 								AS "WS_PrecioGarantia",
		NULL 								AS "WS_TasaGarantia",
		dt."TIFTasa" 						AS "WS_TasaTIF",
		'CARTERA' 							AS "WS_UsoPrecio",
		NULL 								AS "WS_TipoCartera",
		'NORMATIVO'				 			AS "WS_IdTipoCriterioTasa",
		dt."TipoValorizacion" 				AS "WS_TipoValorizacion",
		dt."TieneCorteDeCupon"				AS "WS_TieneCorteDeCupon",
		dt."IdCliente" 						AS "WS_IdCliente",		
		dt."IdPortafolio"					AS "WS_IdPortafolio",	
		dt."INGMontoPrecioPromedioCompra"			AS "WS_BK_INGMontoCompraActualMonedaInstrumento",
		dt."INGMontoATasaCompraActual"				AS "WS_BK_INGMontoATasaCompraActual",	
		dt."INGMontoMercadoActualMonedaInstrumento" AS "WS_BK_INGMontoMercadoActualMonedaInstrumento",	
		dt."INGMontoATasaMercadoActual"			 	AS "WS_BK_INGMontoATasaMercadoActual"
FROM 	"BSCL.Custodia.BD::CUS.Cartera" dt
WHERE	dt."IdEmpresa" = :IdEmpresa and dt."IdEstado" = 'VIG' AND dt."IdClasificacionTipoInstrumento" in ('RV')
		AND dt."IdCliente" = case when ifnull(:IdCliente,0)=0 then dt."IdCliente" else :IdCliente end
		AND dt."IdPortafolio" = CASE WHEN IFNULL(:IdPortafolio,0)=0 THEN dt."IdPortafolio" ELSE :IdPortafolio END
		AND dt."IdTipoInstrumento" = 'RE'
ORDER BY dt."IdCartera" ASC;

		
