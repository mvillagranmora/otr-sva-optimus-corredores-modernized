Select 	EvCapCli."IdEventoCapitalCliente"										AS "NumeroCargoAbono",		
	EvCapCli."IdPortafolio"														AS "IdPortafolio",		
	EvCapCli."IdMoneda"															AS "IdMoneda",		
	'MOVABN'																	AS "IdTipoMovimiento",
	'CIECDV'																	AS "IdConcepto",
	CASE WHEN EvCapCli."ISFUT"='S' OR EvCapCli."RetencionISFUT" = 'S' 
		THEN Concat('Abono por Dividendo en Dinero con ISFUT, Instrumento : ' , EvCapCli."Nemo") 	
	ELSE Concat('Abono por Dividendo en Dinero, Instrumento : ' , EvCapCli."Nemo") END	AS "Descripcion",
	EvCapCli."IdBoveda"															AS "IdBoveda",
	'T0'																		AS "IdCondicionDeLiquidacion",	
	TO_VARCHAR(:FechaSistema,'YYYY-MM-DD')										AS "FechaCondicionDeLiquidacion",	
	CASE WHEN (EvCapCli."ISFUT"='S' OR EvCapCli."RetencionISFUT" = 'S') AND EvCapCli."MontoRetencionISFUT" > 0
		THEN EvCapCli."Total" - EvCapCli."MontoRetencionISFUT" 
	ELSE EvCapCli."Total" END													AS "Monto",
	TO_BIGINT(0)																AS "IdEjecutivo",
	TO_BIGINT(0)																AS "IdSucursal",
	'EVCA'																		AS "IdTipoProducto",			
	'DIVDIN'																	AS "IdTipoOperacion",			
	TO_BIGINT(0)																AS "IdOperacion",				
	TO_VARCHAR(:FechaSistema,'YYYY-MM-DD')										AS "FechaOperacion",			
	TO_VARCHAR(:FechaSistema,'YYYY-MM-DD')										AS "FechaLiquidacionOperacion",	
	'0001-01-01'																AS "FechaVencimientoOperacion",	
	''																			AS "IdMonedaOperacion",			
	TO_DECIMAL(0,26,8) 															AS "MontoOperacion",			
	TO_DECIMAL(0,26,8) 															AS "TipoCambioOperacion",		
	TO_BIGINT(0)																AS "IdContraparte",				
	''																			AS "IdTipoComprobante",			
	TO_BIGINT(0)																AS "IdComprobante",				
	''																			AS "FolioComprobante",
	EvCapCli."IdEventoCapitalCliente"											AS "IdOperacion",
	TO_BIGINT(0)																AS "IdComprobanteDetalle",		
	TO_BIGINT(0)																AS "IdComprobanteFormaDePago",
	Coalesce(EvCapCli."IdTipoEventoCapital",'') 								As "IdTipoMovimientoAsociado",
	Coalesce(EvCapCli."IdEventoCapitalCliente",0) 								As "IdMovimientoAsociado",
	'0001-01-01'																AS "FechaComprobante",			
	TO_BIGINT(0)																AS "IdPersonaPagoCobro",		
	EvCapCli."IdInstrumento"													AS "IdInstrumento",				
	''																			AS "IdTipoInstrumento",
	EvCapCli."Nemo" 															AS "Nemo",
	''																			AS "EsIndicadorBilateral",		
	'N'																			AS "EsCargoAbonoPorTraspaso",	
	TO_BIGINT(0)																AS "IdPortafolioTraspaso",		
	'N'																			AS "GeneroContraparteEnTesoreria",
	'S'																			AS "CertificadoAutomatico",
	(EvCapCli."MontoBeneficioArchivo" - EvCapCli."MontoRetencionArchivo")		AS "MontoMenosRetenido",
	EvCapCli."MontoBeneficioArchivo"											AS "MontoBeneficioArchivo"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EvCapCli
	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS ConCliente
		ON EvCapCli."IdPortafolio" = ConCliente."IdPortafolio"
	LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"			AS ECFP
		ON EvCapCli."IdEventoCapitalCliente" = ECFP."IdEventoCapitalCliente"
	LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 			AS EvCap
		ON EvCap."IdEventoCapital" = EvCapCli."IdEventoCapital"
WHERE EvCapCli."IdEventoCapital" = :IdEventoCapital
  AND EvCapCli."IdTipoEventoCapital" = 'DV'
  AND EvCapCli."Total" > 0

--**********************************************************************   NOMINALES *****************************************************************************  
UNION ALL 

Select 	EvCapCli."IdEventoCapitalCliente"										AS "NumeroCargoAbono",		
	EvCapCli."IdPortafolio"														AS "IdPortafolio",		
	EvCapCli."IdMoneda"															AS "IdMoneda",				
	'MOVCAR'																	AS "IdTipoMovimiento",
	'CIECDV'																	AS "IdConcepto",
	CASE WHEN EvCapCli."ISFUT"='S' OR EvCapCli."RetencionISFUT" = 'S' 
		THEN Concat('Cargo por Dividendo en Dinero con ISFUT, Instrumento : ' , EvCapCli."Nemo") 	
	ELSE Concat('Cargo por Dividendo en Dinero, Instrumento : ' , EvCapCli."Nemo") END	AS "Descripcion",
	EvCapCli."IdBoveda"															AS "IdBoveda",
	'T0'																		AS "IdCondicionDeLiquidacion",	
	TO_VARCHAR(:FechaSistema,'YYYY-MM-DD')										AS "FechaCondicionDeLiquidacion",	
	CASE WHEN EvCapCli."ISFUT"='S' OR EvCapCli."RetencionISFUT" = 'S' 
		THEN ABS(EvCapCli."Total") - ABS(EvCapCli."MontoRetencionISFUT")
	ELSE ABS(EvCapCli."Total") END													AS "Monto",
	TO_BIGINT(0)																AS "IdEjecutivo",
	TO_BIGINT(0)																AS "IdSucursal",
	'EVCA'																		AS "IdTipoProducto",			
	'DIVDIN'																	AS "IdTipoOperacion",			
	TO_BIGINT(0)																AS "IdOperacion",				
	TO_VARCHAR(:FechaSistema,'YYYY-MM-DD')										AS "FechaOperacion",			
	TO_VARCHAR(:FechaSistema,'YYYY-MM-DD')										AS "FechaLiquidacionOperacion",	
	'0001-01-01'																AS "FechaVencimientoOperacion",	
	''																			AS "IdMonedaOperacion",			
	TO_DECIMAL(0,26,8) 															AS "MontoOperacion",			
	TO_DECIMAL(0,26,8) 															AS "TipoCambioOperacion",		
	TO_BIGINT(0)																AS "IdContraparte",				
	''																			AS "IdTipoComprobante",			
	TO_BIGINT(0)																AS "IdComprobante",				
	''																			AS "FolioComprobante",
	EvCapCli."IdEventoCapitalCliente"											AS "IdOperacion",
	TO_BIGINT(0)																AS "IdComprobanteDetalle",		
	TO_BIGINT(0)																AS "IdComprobanteFormaDePago",
	Coalesce(EvCapCli."IdTipoEventoCapital",'') 								As "IdTipoMovimientoAsociado",
	Coalesce(EvCapCli."IdEventoCapitalCliente",0) 								As "IdMovimientoAsociado",
	'0001-01-01'																AS "FechaComprobante",			
	TO_BIGINT(0)																AS "IdPersonaPagoCobro",		
	EvCapCli."IdInstrumento"													AS "IdInstrumento",				
	''																			AS "IdTipoInstrumento",
	EvCapCli."Nemo" 															AS "Nemo",
	''																			AS "EsIndicadorBilateral",		
	'N'																			AS "EsCargoAbonoPorTraspaso",	
	TO_BIGINT(0)																AS "IdPortafolioTraspaso",		
	'N'																			AS "GeneroContraparteEnTesoreria",
	'S'																			AS "CertificadoAutomatico",
	(EvCapCli."MontoBeneficioArchivo" - EvCapCli."MontoRetencionArchivo")		AS "MontoMenosRetenido",
	EvCapCli."MontoBeneficioArchivo"											AS "MontoBeneficioArchivo"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EvCapCli
	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS ConCliente
		ON EvCapCli."IdPortafolio" = ConCliente."IdPortafolio"
	LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"			AS ECFP
		ON EvCapCli."IdEventoCapitalCliente" = ECFP."IdEventoCapitalCliente"
	LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 			AS EvCap
		ON EvCap."IdEventoCapital" = EvCapCli."IdEventoCapital"
WHERE EvCapCli."IdEventoCapital" = :IdEventoCapital
  AND EvCapCli."IdTipoEventoCapital" = 'DV'
  AND EvCapCli."Total" < 0