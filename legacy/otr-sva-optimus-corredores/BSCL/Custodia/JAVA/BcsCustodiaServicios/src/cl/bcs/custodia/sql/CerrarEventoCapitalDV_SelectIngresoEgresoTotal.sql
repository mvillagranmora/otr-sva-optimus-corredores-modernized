SELECT 	row_number() OVER (order by FT."IdBoveda",	PER."IdPersona", FT."IdMoneda", Emisor."IdPersona", FT."TipoSocioNegocio") 	AS "NumeroReferencia"
		,'MOVING'														AS "IdTipoMovimiento"
		,FT."TipoSocioNegocio"											AS "IdTipoPersona"
		,CASE 
			WHEN FT."TipoSocioNegocio" = 'CNT' AND :pNombreEmisor = 'S' AND FT."TipoCartera" != 'GAR' AND FT."DocumentoIdentidad" = Emisor."DocumentoIdentidad" THEN Emisor."IdPersona"
			ELSE PER."IdPersona"
		END																AS "IdPersona"
		,FT."IdPortafolio"  											AS "IdPortafolio"
		,FT."IdMoneda" 													AS "IdMoneda"
		,sum(ABS(FT."Total"))											AS "Monto"	
		,TO_VARCHAR(:FechaSistema,'YYYY-MM-DD')							AS "FechaLiquidacion"
		,'CIECDV'														AS "IdConcepto"
		,FT."TipoSocioNegocio"											AS "IdTipoPersonaTitularPago"
		,CASE 
			WHEN FT."TipoSocioNegocio" = 'CNT' AND :pNombreEmisor = 'S' AND FT."TipoCartera" != 'GAR' AND FT."DocumentoIdentidad" = Emisor."DocumentoIdentidad" THEN Emisor."IdPersona"
			ELSE PER."IdPersona"
		END																AS "IdPersonaTitularPago"	
		,CASE WHEN FT."TipoSocioNegocio" = 'CNT' THEN 0
			ELSE FT."IdPortafolio" END  								AS "IdPortafolioTitularPago"
		,TO_BIGINT(0) 													AS "IdCaja"
		,'' 															AS "SecuenciaAgrupacion"
		,TO_BIGINT(0)													AS "SecuenciaRelacionada"
		,'' 															AS "Glosa"
		,TO_BIGINT(0)													AS "IdEmpleadoDigitador"							
		,TO_BIGINT(0)													AS "IdSucursalEmpleadoDigitador"							
		,'EVCA'															AS "IdTipoProducto"						
		,'DIVDIN'														AS "IdTipoOperacion"					
		,''																AS "IdTipoComprobante"		
		,TO_BIGINT(0)													AS "IdComprobante"			
		,TO_VARCHAR(:FechaSistema,'YYYY-MM-DD')							AS "FechaComprobante"
		,TO_VARCHAR(:FechaSistema,'YYYY-MM-DD')							AS "FechaLiquidacionComprobante"	
		,TO_BIGINT(0)													AS "IdOperacion" 
		,'0001-01-01' 													AS "FechaOperacion"
		,'' 															AS "IdCondicionLiquidacion"
		,'' 															AS "IdBolsa"
		,FT."IdBoveda"													AS "IdBoveda"
		,'' 															AS "IdMonedaOperacion"
		,TO_DECIMAL(0,26,8) 											AS "MontoOperacion"
		,'' 															AS "IdTipoReajuste"
		,'0001-01-01' 													AS "FechaParidad"
		,TO_DECIMAL(0,26,8)  											AS "ValorParidad"
		,TO_BIGINT(0)													AS "FolioCartera"
		,FT."IdInstrumento"												AS "IdInstrumento"
		,FT."IdTipoInstrumento"											AS "IdTipoInstrumento"
		,FT."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento"				
		,'N' 															AS "Bilateral"
		,'N' 															AS "GeneraComprobante"
		,'N' 															AS "AgruparEnComprobante"
		,'N' 															AS "MueveCuentaInversion"
		,'S' 															AS "TitularPago"
		,(Sum(FT."MontoBeneficioArchivo")- Sum(FT."MontoRetencionArchivo"))	AS "MontoMenosMontoRetenido"		
		,Sum(FT."MontoBeneficioArchivo")								AS "MontoBeneficioArchivo"
		,'S'															AS "TienePreferenciaDePago"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"	 					   	AS FT
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"   	AS CSN
		ON (FT."IdSocioNegocio" = CSN."IdSocioDeNegocio")		
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"							AS PER
		ON (CSN."IdPersona" = PER."IdPersona")										
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda"             	AS BO
		ON (FT."IdBoveda" = BO."IdBoveda")
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"							AS PE
		ON (BO."IdPersona" = PE."IdPersona")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"   	AS SN
		ON (PE."IdPersona" = SN."IdPersona")
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 		AS Instru
		on FT."IdInstrumento" = Instru."IdInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" 				AS Emisor
		on  Instru."IdEmisor" = Emisor."IdEmisor"
	LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"		AS ECFP  
		on FT."IdEventoCapitalCliente" = ECFP."IdEventoCapitalCliente"
WHERE  	FT."IdEventoCapital" = :IdEventoCapital
	and (FT."IdEstado" = 'PRO' OR FT."IdEstado" = 'NOT')
	and FT."Total" < 0
	and FT."TipoCartera" <> 'NOM'
	AND (ECFP."IdMedioDePago" <> 'CIN' OR IFNULL(ECFP."IdMedioDePago",'') = '') 
group by 	FT."IdBoveda",
			PER."IdPersona",
			FT."IdPortafolio",
			FT."IdMoneda",
			Emisor."IdPersona",
			Emisor."DocumentoIdentidad",
			FT."DocumentoIdentidad",
			FT."IdInstrumento",
			FT."IdTipoInstrumento",			
			FT."IdClasificacionTipoInstrumento",
			FT."TipoSocioNegocio",
			FT."TipoCartera"
			
UNION ALL
--**********************************************************************   NOMINALES *****************************************************************************

SELECT 	row_number() OVER (order by FT."IdBoveda",	PER."IdPersona", FT."IdMoneda", Emisor."IdPersona", FT."TipoSocioNegocio") 	AS "NumeroReferencia"
		,'MOVING'														AS "IdTipoMovimiento"
		,FT."TipoSocioNegocio"											AS "IdTipoPersona"
		,CASE 
			WHEN FT."TipoSocioNegocio" = 'CNT' AND :pNombreEmisor = 'S' AND FT."TipoCartera" != 'GAR' AND FT."DocumentoIdentidad" = Emisor."DocumentoIdentidad" THEN Emisor."IdPersona"
			ELSE PER."IdPersona"
		END                                                             AS "IdPersona"
		,FT."IdPortafolio"  											AS "IdPortafolio"
		,FT."IdMoneda" 													AS "IdMoneda"
		,sum(ABS(FT."Total"))											AS "Monto"	
		,TO_VARCHAR(:FechaSistema,'YYYY-MM-DD')							AS "FechaLiquidacion"
		,'CIECDV'														AS "IdConcepto"
		,FT."TipoSocioNegocio"											AS "IdTipoPersonaTitularPago"
		,CASE 
			WHEN FT."TipoSocioNegocio" = 'CNT' AND :pNombreEmisor = 'S' AND FT."TipoCartera" != 'GAR' AND FT."DocumentoIdentidad" = Emisor."DocumentoIdentidad" THEN Emisor."IdPersona"
			ELSE PER."IdPersona"
		END																AS "IdPersonaTitularPago"
		,CASE WHEN FT."TipoSocioNegocio" = 'CNT' THEN 0
			ELSE FT."IdPortafolio" END  								AS "IdPortafolioTitularPago"			
		,TO_BIGINT(0) 													AS "IdCaja"
		,'' 															AS "SecuenciaAgrupacion"
		,TO_BIGINT(0)													AS "SecuenciaRelacionada"
		,'' 															AS "Glosa"
		,TO_BIGINT(0)													AS "IdEmpleadoDigitador"							
		,TO_BIGINT(0)													AS "IdSucursalEmpleadoDigitador"							
		,'EVCA'															AS "IdTipoProducto"						
		,'DIVDIN'														AS "IdTipoOperacion"					
		,''																AS "IdTipoComprobante"		
		,TO_BIGINT(0)													AS "IdComprobante"			
		,TO_VARCHAR(:FechaSistema,'YYYY-MM-DD')							AS "FechaComprobante"
		,TO_VARCHAR(:FechaSistema,'YYYY-MM-DD')							AS "FechaLiquidacionComprobante"	
		,TO_BIGINT(0)													AS "IdOperacion" 
		,'0001-01-01' 													AS "FechaOperacion"
		,'' 															AS "IdCondicionLiquidacion"
		,'' 															AS "IdBolsa"
		,FT."IdBoveda"													AS "IdBoveda"
		,'' 															AS "IdMonedaOperacion"
		,TO_DECIMAL(0,26,8) 											AS "MontoOperacion"
		,'' 															AS "IdTipoReajuste"
		,'0001-01-01' 													AS "FechaParidad"
		,TO_DECIMAL(0,26,8)  											AS "ValorParidad"
		,TO_BIGINT(0)													AS "FolioCartera"
		,FT."IdInstrumento"												AS "IdInstrumento"
		,FT."IdTipoInstrumento"											AS "IdTipoInstrumento"
		,FT."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento"				
		,'N' 															AS "Bilateral"
		,'N' 															AS "GeneraComprobante"
		,'N' 															AS "AgruparEnComprobante"
		,'N' 															AS "MueveCuentaInversion"
		,'S' 															AS "TitularPago"
		,(Sum(FT."MontoBeneficioArchivo")- Sum(FT."MontoRetencionArchivo"))	AS "MontoMenosMontoRetenido"		
		,Sum(FT."MontoBeneficioArchivo")								AS "MontoBeneficioArchivo"
		,'S'															AS "TienePreferenciaDePago"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"	 					   	AS FT
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"   	AS CSN
		ON (FT."IdSocioNegocio" = CSN."IdSocioDeNegocio")		
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"							AS PER
		ON (CSN."IdPersona" = PER."IdPersona")										
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda"             	AS BO
		ON (FT."IdBoveda" = BO."IdBoveda")
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"							AS PE
		ON (BO."IdPersona" = PE."IdPersona")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"   	AS SN
		ON (PE."IdPersona" = SN."IdPersona")
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 		AS Instru
		on FT."IdInstrumento" = Instru."IdInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" 				AS Emisor
		on  Instru."IdEmisor" = Emisor."IdEmisor"
	LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"		AS ECFP  
		on FT."IdEventoCapitalCliente" = ECFP."IdEventoCapitalCliente"
WHERE  	FT."IdEventoCapital" = :IdEventoCapital
	and (FT."IdEstado" = 'PRO' OR FT."IdEstado" = 'NOT')
	and FT."Total" < 0
	and FT."TipoCartera" = 'NOM'
	and (ECFP."IdMedioDePago" <> 'CIN' OR IFNULL(ECFP."IdMedioDePago",'') = '') 
group by 	FT."IdBoveda",
			PER."IdPersona",
			FT."IdMoneda",
			Emisor."IdPersona",
			Emisor."DocumentoIdentidad",
			FT."DocumentoIdentidad",
			FT."IdInstrumento",
			FT."IdTipoInstrumento",			
			FT."IdClasificacionTipoInstrumento",
			FT."TipoSocioNegocio",
			FT."IdPortafolio",
			FT."TipoCartera"
