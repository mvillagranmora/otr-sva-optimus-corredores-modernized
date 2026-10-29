Select 	EvCapCli."IdEventoCapitalCliente"										AS "NumeroCargoAbono",		
	EvCapCli."IdPortafolio"														AS "IdPortafolio",		
	EvCapCli."IdMoneda"															AS "IdMoneda",			
	'MOVABN'																	AS "IdTipoMovimiento",
	'REPDIN'																	AS "IdConcepto",
	Concat('Abono por Reparto de Capital, Instrumento : ' , EvCapCli."Nemo")	AS "Descripcion",
	EvCapCli."IdBoveda"															AS "IdBoveda",
	'T0'																		AS "IdCondicionDeLiquidacion",	
	TO_VARCHAR(:FechaSistema,'YYYY-MM-DD')										AS "FechaCondicionDeLiquidacion",
	EvCapCli."Total"															AS "Monto",
	TO_BIGINT(0)																AS "IdEjecutivo",
	TO_BIGINT(0)																AS "IdSucursal",
	'EVCA'																		AS "IdTipoProducto",			
	'REPDIN'																	AS "IdTipoOperacion",			
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
	"IdInstrumento"																AS "IdInstrumento",				
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
	
WHERE EvCapCli."IdEventoCapital" = :IdEventoCapital
  AND EvCapCli."IdTipoEventoCapital" = 'RC'
  AND EvCapCli."Total" > 0
  AND (EvCapCli."IdEstado" = 'PRO' OR EvCapCli."IdEstado" = 'NOT')

--**********************************************************************   NOMINALES *****************************************************************************  
UNION ALL 

Select 	EvCapCli."IdEventoCapitalCliente"										AS "NumeroCargoAbono",		
	EvCapCli."IdPortafolio"														AS "IdPortafolio",		
	EvCapCli."IdMoneda"															AS "IdMoneda",			
	'MOVCAR'																	AS "IdTipoMovimiento",
	'REPDIN'																	AS "IdConcepto",
	Concat('Cargo por Reparto de Capital, Instrumento : ' , EvCapCli."Nemo")	AS "Descripcion",
	EvCapCli."IdBoveda"															AS "IdBoveda",
	'T0'																		AS "IdCondicionDeLiquidacion",	
	TO_VARCHAR(:FechaSistema,'YYYY-MM-DD')										AS "FechaCondicionDeLiquidacion",
	ABS(EvCapCli."Total")														AS "Monto",
	TO_BIGINT(0)																AS "IdEjecutivo",
	TO_BIGINT(0)																AS "IdSucursal",
	'EVCA'																		AS "IdTipoProducto",			
	'REPDIN'																	AS "IdTipoOperacion",			
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
	"IdInstrumento"																AS "IdInstrumento",				
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

WHERE EvCapCli."IdEventoCapital" = :IdEventoCapital
  AND EvCapCli."IdTipoEventoCapital" = 'RC'
  AND EvCapCli."Total" < 0
  AND (EvCapCli."IdEstado" = 'PRO' OR EvCapCli."IdEstado" = 'NOT')