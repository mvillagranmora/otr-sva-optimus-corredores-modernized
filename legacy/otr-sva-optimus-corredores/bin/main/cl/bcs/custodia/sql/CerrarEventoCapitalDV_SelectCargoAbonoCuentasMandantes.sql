Select 	EvCapCli."IdEventoCapitalCliente"															AS "NumeroCargoAbono",		
		EvCapCli."IdPortafolio"																		AS "IdPortafolio",		
		EvCapCli."IdMoneda"																			AS "IdMoneda",			
		'MOVCAR'																					AS "IdTipoMovimiento",
		'CIECDV'																					AS "IdConcepto",
		Concat('Cargo - Pago por Emisor - Dividendo en Dinero, Instrumento : ' , EvCapCli."Nemo")	AS "Descripcion",
		EvCapCli."IdBoveda"																			AS "IdBoveda",
		'T0'																						AS "IdCondicionDeLiquidacion",	
		TO_VARCHAR(EvCapCli."FechaPago",'YYYY-MM-DD')												AS "FechaCondicionDeLiquidacion",
		EvCapCli."Total"																			AS "Monto",
		TO_BIGINT(0)																				AS "IdEjecutivo",
		TO_BIGINT(0)																				AS "IdSucursal",
		''																							AS "IdTipoProducto",			
		''																							AS "IdTipoOperacion",			
		TO_BIGINT(0)																				AS "IdOperacion",				
		TO_VARCHAR(EvCapCli."FechaPago",'YYYY-MM-DD')												AS "FechaOperacion",			
		TO_VARCHAR(EvCapCli."FechaPago",'YYYY-MM-DD')												AS "FechaLiquidacionOperacion",	
		'0001-01-01'																				AS "FechaVencimientoOperacion",	
		''																							AS "IdMonedaOperacion",			
		TO_DECIMAL(0,26,8) 																			AS "MontoOperacion",			
		TO_DECIMAL(0,26,8) 																			AS "TipoCambioOperacion",		
		TO_BIGINT(0)																				AS "IdContraparte",				
		''																							AS "IdTipoComprobante",			
		TO_BIGINT(0)																				AS "IdComprobante",				
		''																							AS "FolioComprobante",
		EvCapCli."IdEventoCapitalCliente"															AS "IdOperacion",
		TO_BIGINT(0)																				AS "IdComprobanteDetalle",		
		TO_BIGINT(0)																				AS "IdComprobanteFormaDePago",
		Coalesce(EvCapCli."IdTipoEventoCapital",'') 								As "IdTipoMovimientoAsociado",
		Coalesce(EvCapCli."IdEventoCapitalCliente",0) 								As "IdMovimientoAsociado",
		'0001-01-01'																				AS "FechaComprobante",			
		TO_BIGINT(0)																				AS "IdPersonaPagoCobro",		
		TO_BIGINT(0)																				AS "IdInstrumento",				
		''																							AS "IdTipoInstrumento",			
		''																							AS "Nemo",						
		''																							AS "EsIndicadorBilateral",		
		'N'																							AS "EsCargoAbonoPorTraspaso",	
		TO_BIGINT(0)																				AS "IdPortafolioTraspaso",		
		'N'																							AS "GeneroContraparteEnTesoreria",
		'S'																							AS "CertificadoAutomatico",
		(EvCapCli."MontoBeneficioArchivo" - EvCapCli."MontoRetencionArchivo")						AS "MontoMenosRetenido",
		EvCapCli."MontoBeneficioArchivo"															AS "MontoBeneficioArchivo"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EvCapCli
	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS ConCliente
		ON EvCapCli."IdPortafolio" = ConCliente."IdPortafolio"
	LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"			AS ECFP
		ON EvCapCli."IdEventoCapitalCliente" = ECFP."IdEventoCapitalCliente"
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" 	AS Efp 
		ON ((EvCapCli."IdPortafolio" 	= Efp."IdPortafolio")
		AND Efp."IdEstado" 				= 'ACT'
		AND Efp."IdTipoOperacion" 		= 'DIVDIN'
		AND Efp."PorDefecto" 			= 'S')
	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" AS CuentaCustodio
		ON EvCapCli."IdPortafolio" 	= CuentaCustodio."IdPortafolio"
		AND CuentaCustodio."IdEstado" = 'ACT'
		AND CuentaCustodio."IdEstadoAutorizacion" = 'AUT'
		AND CuentaCustodio."LiquidaDividendos" = 'S'
WHERE EvCapCli."IdEventoCapital" = :IdEventoCapital
  AND EvCapCli."IdTipoEventoCapital" = 'DV'
  AND EvCapCli."PagaClienteCorredor" = 'CL'
  AND EvCapCli."Total" > 0