SELECT 	FT."IdEventoCapitalCliente"											AS "NumeroReferencia"
		,'MOVEGR' 															AS "IdTipoMovimiento"
		,FT."TipoSocioNegocio"  											AS "IdTipoPersona"
		,SN."IdPersona"														AS "IdPersona"
		,FT."IdPortafolio"													AS "IdPortafolio"
		,FT."IdMoneda" 														AS "IdMoneda"
		,CASE WHEN (FT."ISFUT"='S' OR FT."RetencionISFUT" = 'S') AND FT."MontoRetencionISFUT" > 0
		THEN SUM(FT."Total" - FT."MontoRetencionISFUT" )
		ELSE SUM(FT."Total") END											AS "Monto"	
		,TO_VARCHAR(:FechaSistema,'YYYY-MM-DD')								AS "FechaLiquidacion"
		,'CIECDV'															AS "IdConcepto"
		,FT."TipoSocioNegocio"												AS "IdTipoPersonaTitularPago"
		,SN."IdPersona"														AS "IdPersonaTitularPago"				
		,FT."IdPortafolio"													AS "IdPortafolioTitularPago"			
		,TO_BIGINT(0) 														AS "IdCaja"
		,'' 																AS "SecuenciaAgrupacion"
		,TO_BIGINT(0) 														AS "SecuenciaRelacionada"
		,'' 																AS "Glosa"
		,TO_BIGINT(0) 														AS "IdEmpleadoDigitador"							
		,TO_BIGINT(0) 														AS "IdSucursalEmpleadoDigitador"							
		,'EVCA'																AS "IdTipoProducto"						
		,'DIVDIN'															AS "IdTipoOperacion"					
		,''																	AS "IdTipoComprobante"		
		,TO_BIGINT(0) 														AS "IdComprobante"			
		,TO_VARCHAR(:FechaSistema,'YYYY-MM-DD')								AS "FechaComprobante"
		,TO_VARCHAR(:FechaSistema,'YYYY-MM-DD')								AS "FechaLiquidacionComprobante"	
		,FT."IdEventoCapitalCliente" 										AS "IdOperacion" 
		,'0001-01-01' 														AS "FechaOperacion"
		,'' 																AS "IdCondicionLiquidacion"
		,'' 																AS "IdBolsa"
		, FT."IdBoveda"														AS "IdBoveda"
		,'' 																AS "IdMonedaOperacion"
		,TO_DECIMAL(0,26,8)  												AS "MontoOperacion"
		,'' 																AS "IdTipoReajuste"
		,'0001-01-01' 														AS "FechaParidad"
		,TO_DECIMAL(0,26,8)   												AS "ValorParidad"
		,TO_BIGINT(0)  														AS "FolioCartera"
		,FT."IdInstrumento"													AS "IdInstrumento"
		,FT."IdTipoInstrumento"												AS "IdTipoInstrumento"
		,FT."IdClasificacionTipoInstrumento"								AS "IdClasificacionTipoInstrumento"				
		,'N' 																AS "Bilateral"
		,'N' 																AS "GeneraComprobante"		
		,'N' 																AS "AgruparEnComprobante"
		,'N' 																AS "MueveCuentaInversion"
		,'S' 																AS "TitularPago"
		,(Sum(FT."MontoBeneficioArchivo")- Sum(FT."MontoRetencionArchivo"))	AS "MontoMenosMontoRetenido"
		,Sum(FT."MontoBeneficioArchivo")									AS "MontoBeneficioArchivo"
		,CASE WHEN ECFP."IdPreferenciaPagoPortafolio" > 0
					THEN 'S'
					ELSE 'N'
		END																	AS "TienePreferenciaDePago"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"							AS FT												
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"		AS SN 	 ON (FT."IdSocioNegocio" = SN."IdSocioDeNegocio")

LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"			AS ECFP  ON FT."IdEventoCapitalCliente" = ECFP."IdEventoCapitalCliente"
WHERE  	FT."IdEventoCapital" = :IdEventoCapital
	and FT."Total" > 0 AND FT."TipoSocioNegocio" ='CLI'
	and (FT."IdEstado" = 'PRO' OR FT."IdEstado" = 'NOT')
	and (ECFP."IdMedioDePago" <> 'CIN' OR ifnull(ECFP."IdMedioDePago",'') = '') 
group by  FT."IdEventoCapitalCliente", FT."IdPortafolio", SN."IdPersona", FT."IdMoneda", FT."IdBoveda", ECFP."IdPreferenciaPagoPortafolio"
	, FT."IdInstrumento", FT."IdTipoInstrumento", FT."IdClasificacionTipoInstrumento", FT."TipoSocioNegocio", FT."ISFUT", FT."RetencionISFUT", FT."MontoRetencionISFUT"
	

--**********************************************************************   NOMINALES *****************************************************************************
 	
UNION ALL


SELECT 	FT."IdEventoCapitalCliente"											AS "NumeroReferencia"
		,'MOVEGR' 															AS "IdTipoMovimiento"
		,FT."TipoSocioNegocio"  											AS "IdTipoPersona"
		,SN."IdPersona"														AS "IdPersona"
		,FT."IdPortafolio"													AS "IdPortafolio"
		,FT."IdMoneda" 														AS "IdMoneda"
		,CASE WHEN (FT."ISFUT"='S' OR FT."RetencionISFUT" = 'S') AND FT."MontoRetencionISFUT" > 0
		THEN SUM(FT."Total" - FT."MontoRetencionISFUT") 
		ELSE SUM(FT."Total") END											AS "Monto"		
		,TO_VARCHAR(:FechaSistema,'YYYY-MM-DD')								AS "FechaLiquidacion"
		,'CIECDV'															AS "IdConcepto"
		,FT."TipoSocioNegocio"												AS "IdTipoPersonaTitularPago"
		,SN."IdPersona"														AS "IdPersonaTitularPago"				
		,FT."IdPortafolio"													AS "IdPortafolioTitularPago"			
		,TO_BIGINT(0) 														AS "IdCaja"
		,'' 																AS "SecuenciaAgrupacion"
		,TO_BIGINT(0) 														AS "SecuenciaRelacionada"
		,'' 																AS "Glosa"
		,TO_BIGINT(0) 														AS "IdEmpleadoDigitador"							
		,TO_BIGINT(0) 														AS "IdSucursalEmpleadoDigitador"							
		,'EVCA'																AS "IdTipoProducto"						
		,'DIVDIN'															AS "IdTipoOperacion"					
		,''																	AS "IdTipoComprobante"		
		,TO_BIGINT(0) 														AS "IdComprobante"			
		,TO_VARCHAR(:FechaSistema,'YYYY-MM-DD')								AS "FechaComprobante"
		,TO_VARCHAR(:FechaSistema,'YYYY-MM-DD')								AS "FechaLiquidacionComprobante"	
		,FT."IdEventoCapitalCliente"										AS "IdOperacion" 
		,'0001-01-01' 														AS "FechaOperacion"
		,'' 																AS "IdCondicionLiquidacion"
		,'' 																AS "IdBolsa"
		, FT."IdBoveda"														AS "IdBoveda"
		,'' 																AS "IdMonedaOperacion"
		,TO_DECIMAL(0,26,8)  												AS "MontoOperacion"
		,'' 																AS "IdTipoReajuste"
		,'0001-01-01' 														AS "FechaParidad"
		,TO_DECIMAL(0,26,8)   												AS "ValorParidad"
		,TO_BIGINT(0)  														AS "FolioCartera"
		,FT."IdInstrumento"													AS "IdInstrumento"
		,FT."IdTipoInstrumento"												AS "IdTipoInstrumento"
		,FT."IdClasificacionTipoInstrumento"								AS "IdClasificacionTipoInstrumento"				
		,'N' 																AS "Bilateral"
		,'N' 																AS "GeneraComprobante"
		,'N' 																AS "AgruparEnComprobante"
		,'N' 																AS "MueveCuentaInversion"
		,'S' 																AS "TitularPago"
		,(Sum(FT."MontoBeneficioArchivo")- Sum(FT."MontoRetencionArchivo"))	AS "MontoMenosMontoRetenido"
		,Sum(FT."MontoBeneficioArchivo")									AS "MontoBeneficioArchivo"
		,'N'																AS "TienePreferenciaDePago"
		
		
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"							AS FT												
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"	AS SN
		ON (FT."IdSocioNegocio" = SN."IdSocioDeNegocio")
	LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"		AS ECFP  
		ON FT."IdEventoCapitalCliente" = ECFP."IdEventoCapitalCliente"
WHERE  	FT."IdEventoCapital" = :IdEventoCapital
	and FT."Total" > 0
	AND FT."TipoSocioNegocio" = 'CNT' 
	and (FT."IdEstado" = 'PRO' OR FT."IdEstado" = 'NOT')
	and (ECFP."IdMedioDePago" <> 'CIN' OR ifnull(ECFP."IdMedioDePago",'') = '') 
group by  FT."IdEventoCapitalCliente", FT."IdPortafolio", SN."IdPersona", FT."IdMoneda", FT."IdBoveda"
	, FT."IdInstrumento", FT."IdTipoInstrumento", FT."IdClasificacionTipoInstrumento", FT."TipoSocioNegocio", FT."ISFUT", FT."RetencionISFUT", FT."MontoRetencionISFUT"