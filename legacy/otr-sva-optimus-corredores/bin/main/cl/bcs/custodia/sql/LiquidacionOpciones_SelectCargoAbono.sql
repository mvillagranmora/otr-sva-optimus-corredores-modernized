Select 	 
	"BSCL.Custodia.BD.Secuencias::NumeroReferenciaOpciones".NEXTVAL				AS "NumeroCargoAbono",		
	SOR."PortafolioDerivados"													AS "IdPortafolio",		
	CASE 
		WHEN SOR."PerdidaGanancia"  > 0 THEN 'MOVABN' 
		ELSE 'MOVCAR' 
	END 																		AS "IdTipoMovimiento",
	CASE
	WHEN SOR."SaldoEjercicio" <> 0 THEN 		
			CASE 
				WHEN SOR."PerdidaGanancia"  > 0 THEN 	'ABEJ' 
			ELSE 										'CAEJ'
		END 
	WHEN SOR."SaldoSorteo" <> 0 THEN
			CASE 
				WHEN SOR."PerdidaGanancia"  > 0 THEN 	'ABSO' 
			ELSE 										'CASO'
		END	
	END																			AS "IdConcepto",
		
		
	Concat('Liquidación Opciones, Instrumento : ' , SOR."Serie")				AS "Descripcion",
   'T0'																			AS "IdCondicionDeLiquidacion",	
	TO_VARCHAR(SOR."FechaVencimiento",'YYYY-MM-DD')								AS "FechaCondicionDeLiquidacion",
	ABS(SOR."PerdidaGanancia")													AS "Monto",
	TO_BIGINT(0)																AS "IdEjecutivo",
	TO_BIGINT(0)																AS "IdSucursal",
	''																			AS "IdTipoProducto",			
	CASE 
		WHEN SOR."PerdidaGanancia"  > 0 THEN 	'LIDERE' 
		ELSE 									'LIDEPA'
	END 																		AS "IdTipoOperacion",	
	TO_VARCHAR(SOR."Fecha",'YYYY-MM-DD')										AS "FechaOperacion",
	TO_VARCHAR(SOR."FechaVencimiento",'YYYY-MM-DD')								AS "FechaLiquidacionOperacion",	
	TO_VARCHAR(SOR."FechaVencimiento",'YYYY-MM-DD')								AS "FechaVencimientoOperacion",	
	''																			AS "IdMonedaOperacion",			
	TO_DECIMAL(0,26,8) 															AS "MontoOperacion",			
	TO_DECIMAL(0,26,8) 															AS "TipoCambioOperacion",		
	TO_BIGINT(0)																AS "IdContraparte",				
	''																			AS "IdTipoComprobante",			
	TO_BIGINT(0)																AS "IdComprobante",				
	''																			AS "FolioComprobante",
	TO_BIGINT(0)																AS "IdComprobanteDetalle",		
	TO_BIGINT(0)																AS "IdComprobanteFormaDePago",
	COALESCE(SOR."EsPutEsCall",'') 												AS  "IdTipoMovimientoAsociado",
	TO_BIGINT(1)																AS "IdMovimientoAsociado", 
	TO_VARCHAR(SOR."Fecha",'YYYY-MM-DD')										AS "FechaComprobante",
	TO_BIGINT(0)																AS "IdPersonaPagoCobro",		
	INST."IdInstrumento"														AS "IdInstrumento",				
	''																			AS "IdTipoInstrumento",
	SOR."Serie" 																AS "Nemo",
	''																			AS "EsIndicadorBilateral",		
	'N'																			AS "EsCargoAbonoPorTraspaso",	
	TO_BIGINT(0)																AS "IdPortafolioTraspaso",		
	'N'																			AS "GeneroContraparteEnTesoreria",
	'S'																			AS "CertificadoAutomatico",
	CAR."IdCartera"																AS "IdOperacion" 
FROM "BSCL.Custodia.BD::CUS.SorteoContratos"							AS SOR
	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 		AS CLI
		ON SOR."PortafolioDerivados" = CLI."IdPortafolio"
	INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 	AS INST
		ON (INST."Nemotecnico" = SOR."Serie")
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" 			AS CAR
		ON (CAR."IdPortafolio" 				= SOR."PortafolioDerivados"	AND
			CAR."Nemotecnico"				= SOR."Serie"				AND 
			CAR."FechaOperacion"			= SOR."Fecha"				AND
			CAR."FechaVencimientoOperacion"	= SOR."FechaVencimiento"
			)		
WHERE	SOR."Fecha" 							= :FechaSistema
  	AND SOR."PerdidaGanancia" 					<> 0
	AND CAR."IdClasificacionTipoInstrumento" 	= 'DV'
	AND CAR."IdTipoProducto" 					!= 'MEFW'
	AND (SUBSTRING(CAR."IdTipoOperacion", 0, 4) = 'COMO' OR SUBSTRING(CAR."IdTipoOperacion", 0, 4) = 'VENO')  
  