SELECT  
		'CC'															AS "IdTipoEventoCapital"
		,Carte."INGNumeroCupon"											AS "Secuencia"	
		,TO_BIGINT(0)													AS "IdSocioNegocio"
		,'CNT'			   												AS "TipoSocioNegocio"
		,TO_BIGINT(0)													AS "IdPortafolio"
		,'R'															AS "IdTipoDocumentoIdentidad"
		,IFNULL(DocIde."DocumentoIdentidad",'')							AS "DocumentoIdentidad"
		,CASE IFNULL(DocIde."DocumentoIdentidad",'')
			WHEN '' THEN 'NO EXISTE SOCIO NEGOCIO CONTRAPARTE: '||Instru."RazonSocial"
			ELSE IFNULL(Instru."RazonSocial",'')
		END																AS "NombreCliente"
		,Carte."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento"
		,Carte."IdTipoInstrumento"										AS "IdTipoInstrumento"
		,Carte."IdInstrumento"											AS "IdInstrumento"
		,Carte."Nemotecnico"											AS "Nemo"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,''																AS "NemoNuevo"
		,Carte."INGFechaPagoCorteCupon"									AS "FechaCierre"
		,Carte."INGFechaPagoCorteCupon"									AS "FechaPago"		
		,CASE WHEN Carte."IdEstado" = 'VIG' 
				THEN SUM("Saldo") * -1 
				ELSE SUM("Cantidad") * -1								END AS "CantidadDerecho"
		,'CUS'															AS "TipoCartera"
		,TO_DECIMAL(0,26,8)												AS "CantidadNueva"
		,CASE WHEN Carte."INGIdMonedaPagoCupon" = 'CLP' THEN SUM(Carte."INGMontoCapitalCorteCupon") *-1 ELSE SUM(Carte."INGMontoCapitalCorteCuponMonedaInstrumento") * -1 END AS "MontoCapital"	
		,CASE WHEN Carte."INGIdMonedaPagoCupon" = 'CLP' THEN SUM(Carte."INGMontoInteresCorteCupon") * -1 ELSE SUM(Carte."INGMontoInteresCorteCuponMonedaInstrumento") * -1 END AS "MontoInteres"
		,CASE WHEN Carte."INGIdMonedaPagoCupon" = 'CLP' THEN SUM(Carte."INGMontoCorteCupon" * -1) ELSE SUM(Carte."INGMontoCorteCuponMonedaInstrumento" * -1) END AS "Total"
		,Carte."INGIdMonedaPagoCupon"									AS "IdMoneda"
		,Carte."INGNumeroCupon"											AS "SecuenciaCupon"	
		,TO_BIGINT(0)													AS "IdEmpleadoEjecutivoPortafolio"
		,''																AS "CodigoUsuarioAgente"	
		,''																AS "NombreAgente"
		,''																AS "CodigoSucursal"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraIngresoRegistro"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraUltimaActualizacion"			
		,CASE IFNULL(DocIde."DocumentoIdentidad",'')
			WHEN '' THEN 'PEN'
			ELSE 'VIG'
			END															AS "IdEstado"
		,Carte."IdBoveda"												AS "IdBoveda"
		,Carte."IdBolsa"												AS "IdBolsa"
		,''																AS "IdConceptoGarantia"
		,''																AS "IdTipoOperacionGarantia"
		,SUM(Carte."INGMontoReajusteCorteCupon")						AS "MontoReajuste"
		,'SIS'															AS "Origen"
		,''																AS "IdTipoReajuste"
		,TO_DECIMAL(0,26,8)												AS "INGMontoInteresCorteCupon"
		,''	AS "TipoOperacion"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"							AS Carte
LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 				AS Instru ON Carte."IdInstrumento" = Instru."IdInstrumento"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad"	AS DocIde ON Instru."DocumentoIdentidadEmisor" = DocIde."DocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"		AS SocNeg ON DocIde."IdPersona" = SocNeg."IdPersona"
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 			EC
	ON 	(   Carte."IdInstrumento" 		 = EC."IdInstrumento" 			AND 
			Carte."IdTipoInstrumento" 	 = EC."IdTipoInstrumento"		AND
			EC."IdTipoEventoCapital"	 = 'CC'                         AND			
			Carte."INGFechaPagoCorteCupon" = EC."FechaLimite"           AND
			EC."IdEstado"               <> 'ANU'
		)

WHERE	Carte."IdClasificacionTipoInstrumento"	= 'RF'					
AND 	Carte."INGFechaPagoCorteCupon"			BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta	
AND	    Carte."IdEstado" 					IN('VIG','VCD')					
AND	    Carte."IdBoveda" 						= 'CIA'			
AND	    EC."IdTipoInstrumento" 					IS NULL
AND   	Carte."INGFechaCorteCupon"				= Carte."INGFechaPagoCorteCupon"		

AND     Carte."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN Carte."IdInstrumento" ELSE :IdInstrumento END)


AND     (CASE -- Letras hipotecarias cuyo nemo tiene "*" o "&" no generan pago cupon sino cambio de nemo
			WHEN Carte."IdTipoInstrumento" = 'LTH' AND LOCATE(Carte."Nemotecnico", '*') > 0 THEN 'CAMBIO_NEMO' 
			WHEN Carte."IdTipoInstrumento" = 'LTH' AND LOCATE(Carte."Nemotecnico", '&') > 0 THEN 'CAMBIO_NEMO'
			ELSE 'CORTE_CUPON'
		END) = 'CORTE_CUPON' 	
	
	
GROUP BY 
	DocIde."DocumentoIdentidad",
	Instru."RazonSocial",
	Carte."IdClasificacionTipoInstrumento",	
	Carte."IdTipoInstrumento",		
	Carte."IdInstrumento",
	Carte."Nemotecnico",
	Carte."INGFechaPagoCorteCupon",
	Carte."INGIdMonedaPagoCupon",						
	Carte."INGNumeroCupon",					
	Carte."IdBoveda",
	Carte."IdBolsa",
	Carte."IdEstado"

	UNION ALL
	--Cartera Historica
	SELECT 
		'CC'															AS "IdTipoEventoCapital"
		,Carte."INGNumeroCupon"											AS "Secuencia"	
		,TO_BIGINT(0)													AS "IdSocioNegocio"
		,'CNT'			   												AS "TipoSocioNegocio"
		,TO_BIGINT(0)													AS "IdPortafolio"
		,'R'															AS "IdTipoDocumentoIdentidad"
		,IFNULL(DocIde."DocumentoIdentidad",'')							AS "DocumentoIdentidad"
		,CASE IFNULL(DocIde."DocumentoIdentidad",'')
			WHEN '' THEN 'NO EXISTE SOCIO NEGOCIO CONTRAPARTE: '||Instru."RazonSocial"
			ELSE IFNULL(Instru."RazonSocial",'')
		END																AS "NombreCliente"
		,Carte."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento"
		,Carte."IdTipoInstrumento"										AS "IdTipoInstrumento"
		,Carte."IdInstrumento"											AS "IdInstrumento"
		,Carte."Nemotecnico"											AS "Nemo"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,''																AS "NemoNuevo"
		,Carte."INGFechaPagoCorteCupon"									AS "FechaCierre"
		,Carte."INGFechaPagoCorteCupon"									AS "FechaPago"		
		,CASE WHEN Carte."IdEstado" = 'VIG' 
				THEN SUM(Carte."INGCantidad" - Carte."EGRCantidad") * -1 
				ELSE SUM("Cantidad") * -1								END AS "CantidadDerecho"
		,'CUS'															AS "TipoCartera"
		,TO_DECIMAL(0,26,8)												AS "CantidadNueva"
		,CASE WHEN Carte."INGIdMonedaPagoCupon" = 'CLP' THEN SUM(Carte."INGMontoCapitalCorteCupon") *-1 ELSE SUM(Carte."INGMontoCapitalCorteCuponMonedaInstrumento") * -1 END AS "MontoCapital"	
		,CASE WHEN Carte."INGIdMonedaPagoCupon" = 'CLP' THEN SUM(Carte."INGMontoInteresCorteCupon") * -1 ELSE SUM(Carte."INGMontoInteresCorteCuponMonedaInstrumento") * -1 END AS "MontoInteres"
		,CASE WHEN Carte."INGIdMonedaPagoCupon" = 'CLP' THEN SUM(Carte."INGMontoCorteCupon" * -1) ELSE SUM(Carte."INGMontoCorteCuponMonedaInstrumento" * -1) END AS "Total"
		,Carte."INGIdMonedaPagoCupon"									AS "IdMoneda"
		,Carte."INGNumeroCupon"											AS "SecuenciaCupon"	
		,TO_BIGINT(0)													AS "IdEmpleadoEjecutivoPortafolio"
		,''																AS "CodigoUsuarioAgente"	
		,''																AS "NombreAgente"
		,''																AS "CodigoSucursal"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraIngresoRegistro"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraUltimaActualizacion"			
		,CASE IFNULL(DocIde."DocumentoIdentidad",'')
			WHEN '' THEN 'PEN'
			ELSE 'VIG'
			END															AS "IdEstado"
		,Carte."IdBoveda"												AS "IdBoveda"
		,Carte."IdBolsa"												AS "IdBolsa"
		,''																AS "IdConceptoGarantia"
		,''																AS "IdTipoOperacionGarantia"
		,SUM(Carte."INGMontoReajusteCorteCupon")						AS "MontoReajuste"
		,'SIS'															AS "Origen"
		,''																AS "IdTipoReajuste"
		,TO_DECIMAL(0,26,8)												AS "INGMontoInteresCorteCupon"
		,''	AS "TipoOperacion"
FROM "BSCL.Custodia.BD::HIS.Cartera"											AS Carte
LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 				AS Instru ON Carte."IdInstrumento" = Instru."IdInstrumento"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad"	AS DocIde ON Instru."DocumentoIdentidadEmisor" = DocIde."DocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"		AS SocNeg ON DocIde."IdPersona" = SocNeg."IdPersona"
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 			EC
	ON 	(   Carte."IdInstrumento" 		 = EC."IdInstrumento" 			AND 
			Carte."IdTipoInstrumento" 	 = EC."IdTipoInstrumento"		AND
			EC."IdTipoEventoCapital"	 = 'CC'                         AND			
			Carte."INGFechaPagoCorteCupon" = EC."FechaLimite"           AND
			EC."IdEstado"               <> 'ANU'
		)

WHERE	Carte."IdClasificacionTipoInstrumento"	= 'RF'					
AND 	Carte."INGFechaPagoCorteCupon"			BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta	
AND	    Carte."IdEstado" 						IN('VIG','VCD')					
AND	    Carte."IdBoveda" 						= 'CIA'			
AND	    EC."IdTipoInstrumento" 					IS NULL
AND	  	Carte."INGFechaCorteCupon"				<> Carte."INGFechaPagoCorteCupon"
AND   	Carte."FechaRespaldo"					= Carte."INGFechaCorteCupon" 		

AND     Carte."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN Carte."IdInstrumento" ELSE :IdInstrumento END)


AND     (CASE -- Letras hipotecarias cuyo nemo tiene "*" o "&" no generan pago cupon sino cambio de nemo
			WHEN Carte."IdTipoInstrumento" = 'LTH' AND LOCATE(Carte."Nemotecnico", '*') > 0 THEN 'CAMBIO_NEMO' 
			WHEN Carte."IdTipoInstrumento" = 'LTH' AND LOCATE(Carte."Nemotecnico", '&') > 0 THEN 'CAMBIO_NEMO'
			ELSE 'CORTE_CUPON'
		END) = 'CORTE_CUPON' 	
	
	
GROUP BY 
	DocIde."DocumentoIdentidad",
	Instru."RazonSocial",
	Carte."IdClasificacionTipoInstrumento",	
	Carte."IdTipoInstrumento",		
	Carte."IdInstrumento",
	Carte."Nemotecnico",
	Carte."INGFechaPagoCorteCupon",
	Carte."INGIdMonedaPagoCupon",						
	Carte."INGNumeroCupon",					
	Carte."IdBoveda",
	Carte."IdBolsa",
	Carte."IdEstado"
	
	UNION ALL
--Garantia Contraparte
SELECT 
		'CC'															AS "IdTipoEventoCapital"
		,GTA."INGNumeroCupon"											AS "Secuencia"	
		,TO_BIGINT(0)													AS "IdSocioNegocio"
		,'CNT'			   												AS "TipoSocioNegocio"
		,TO_BIGINT(0)													AS "IdPortafolio"
		,'R'															AS "IdTipoDocumentoIdentidad"
		,IFNULL(DocIde."DocumentoIdentidad",'')							AS "DocumentoIdentidad"
		,CASE IFNULL(DocIde."DocumentoIdentidad",'')
			WHEN '' THEN 'NO EXISTE SOCIO NEGOCIO CONTRAPARTE: '||Instru."RazonSocial"
			ELSE IFNULL(Instru."RazonSocial",'')
		END																AS "NombreCliente"
		,Carte."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento"
		,Carte."IdTipoInstrumento"										AS "IdTipoInstrumento"
		,Carte."IdInstrumento"											AS "IdInstrumento"
		,Carte."Nemotecnico"											AS "Nemo"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,''																AS "NemoNuevo"
		,Carte."INGFechaPagoCorteCupon"									AS "FechaCierre"
		,Carte."INGFechaPagoCorteCupon"									AS "FechaPago"		
		,SUM(GTA."Cantidad") * -1										AS "CantidadDerecho"
		,'CUS'															AS "TipoCartera"
		,TO_DECIMAL(0,26,8)												AS "CantidadNueva"
		,CASE WHEN Carte."INGIdMonedaPagoCupon" = 'CLP' THEN SUM(Carte."INGMontoCapitalCorteCupon") *-1 ELSE SUM(Carte."INGMontoCapitalCorteCuponMonedaInstrumento") * -1 END AS "MontoCapital"	
		,CASE WHEN Carte."INGIdMonedaPagoCupon" = 'CLP' THEN SUM(Carte."INGMontoInteresCorteCupon") * -1 ELSE SUM(Carte."INGMontoInteresCorteCuponMonedaInstrumento") * -1 END AS "MontoInteres"
		,CASE WHEN Carte."INGIdMonedaPagoCupon" = 'CLP' THEN SUM(Carte."INGMontoCorteCupon" * -1) ELSE SUM(Carte."INGMontoCorteCuponMonedaInstrumento" * -1) END AS "Total"
		,Carte."INGIdMonedaPagoCupon"									AS "IdMoneda"
		,GTA."INGNumeroCupon"											AS "SecuenciaCupon"	
		,TO_BIGINT(0)													AS "IdEmpleadoEjecutivoPortafolio"
		,''																AS "CodigoUsuarioAgente"	
		,''																AS "NombreAgente"
		,''																AS "CodigoSucursal"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraIngresoRegistro"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraUltimaActualizacion"			
		,CASE IFNULL(DocIde."DocumentoIdentidad",'')
			WHEN '' THEN 'PEN'
			ELSE 'VIG'
			END															AS "IdEstado"
		,Carte."IdBoveda"												AS "IdBoveda"
		,Carte."IdBolsa"												AS "IdBolsa"
		,''																AS "IdConceptoGarantia"
		,''																AS "IdTipoOperacionGarantia"
		,SUM(Carte."INGMontoReajusteCorteCupon")						AS "MontoReajuste"
		,'SIS'															AS "Origen"
		,''																AS "IdTipoReajuste"
		,TO_DECIMAL(0,26,8)												AS "INGMontoInteresCorteCupon"
		,''	AS "TipoOperacion"
FROM  "BSCL.Custodia.BD::GTA.Garantia" 											AS GTA
INNER JOIN	"BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"						AS Carte ON (Carte."FolioCartera" = GTA."FolioCarteraOrigen" AND Carte."Nemotecnico" = GTA."Nemotecnico")
LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 				AS Instru ON Carte."IdInstrumento" = Instru."IdInstrumento"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad"	AS DocIde ON Instru."DocumentoIdentidadEmisor" = DocIde."DocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"		AS SocNeg ON DocIde."IdPersona" = SocNeg."IdPersona"
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 			EC
	ON 	(   Carte."IdInstrumento" 		 = EC."IdInstrumento" 			AND 
			Carte."IdTipoInstrumento" 	 = EC."IdTipoInstrumento"		AND
			EC."IdTipoEventoCapital"	 = 'CC'                         AND			
			Carte."INGFechaPagoCorteCupon" = EC."FechaLimite"           AND
			EC."IdEstado"               <> 'ANU'
		)

WHERE	Carte."IdClasificacionTipoInstrumento"	= 'RF'					
AND 	GTA."INGFechaPagoCorteCupon"			BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta	

AND 	GTA."IdEstado" = 'VIG'             -- La garantia debe estar Vigente
AND 	Carte."IdEstado" IN ('LIQ', 'RPA')  -- La cartera que genero la garantia debe haber quedado liquidada o retirada parcialmente

AND	    Carte."IdBoveda" 						= 'CIA'			
AND	    EC."IdTipoInstrumento" 					IS NULL
AND   	Carte."INGFechaCorteCupon"				= Carte."INGFechaPagoCorteCupon"		

AND     Carte."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN Carte."IdInstrumento" ELSE :IdInstrumento END)


AND     (CASE -- Letras hipotecarias cuyo nemo tiene "*" o "&" no generan pago cupon sino cambio de nemo
			WHEN Carte."IdTipoInstrumento" = 'LTH' AND LOCATE(Carte."Nemotecnico", '*') > 0 THEN 'CAMBIO_NEMO' 
			WHEN Carte."IdTipoInstrumento" = 'LTH' AND LOCATE(Carte."Nemotecnico", '&') > 0 THEN 'CAMBIO_NEMO'
			ELSE 'CORTE_CUPON'
		END) = 'CORTE_CUPON' 	
	
	
GROUP BY 
	DocIde."DocumentoIdentidad",
	Instru."RazonSocial",
	Carte."IdClasificacionTipoInstrumento",	
	Carte."IdTipoInstrumento",		
	Carte."IdInstrumento",
	Carte."Nemotecnico",
	Carte."INGFechaPagoCorteCupon",
	Carte."INGIdMonedaPagoCupon",						
	GTA."INGNumeroCupon",					
	Carte."IdBoveda",
	Carte."IdBolsa",
	Carte."IdEstado"
	
	UNION ALL
--Garantia Historica
SELECT 
		'CC'															AS "IdTipoEventoCapital"
		,GTA."INGNumeroCupon"											AS "Secuencia"	
		,TO_BIGINT(0)													AS "IdSocioNegocio"
		,'CNT'			   												AS "TipoSocioNegocio"
		,TO_BIGINT(0)													AS "IdPortafolio"
		,'R'															AS "IdTipoDocumentoIdentidad"
		,IFNULL(DocIde."DocumentoIdentidad",'')							AS "DocumentoIdentidad"
		,CASE IFNULL(DocIde."DocumentoIdentidad",'')
			WHEN '' THEN 'NO EXISTE SOCIO NEGOCIO CONTRAPARTE: '||Instru."RazonSocial"
			ELSE IFNULL(Instru."RazonSocial",'')
		END																AS "NombreCliente"
		,Carte."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento"
		,Carte."IdTipoInstrumento"										AS "IdTipoInstrumento"
		,Carte."IdInstrumento"											AS "IdInstrumento"
		,Carte."Nemotecnico"											AS "Nemo"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,''																AS "NemoNuevo"
		,Carte."INGFechaPagoCorteCupon"									AS "FechaCierre"
		,Carte."INGFechaPagoCorteCupon"									AS "FechaPago"		
		,SUM(GTA."Cantidad") * -1										AS "CantidadDerecho"
		,'CUS'															AS "TipoCartera"
		,TO_DECIMAL(0,26,8)												AS "CantidadNueva"
		,CASE WHEN Carte."INGIdMonedaPagoCupon" = 'CLP' THEN SUM(Carte."INGMontoCapitalCorteCupon") *-1 ELSE SUM(Carte."INGMontoCapitalCorteCuponMonedaInstrumento") * -1 END AS "MontoCapital"	
		,CASE WHEN Carte."INGIdMonedaPagoCupon" = 'CLP' THEN SUM(Carte."INGMontoInteresCorteCupon") * -1 ELSE SUM(Carte."INGMontoInteresCorteCuponMonedaInstrumento") * -1 END AS "MontoInteres"
		,CASE WHEN Carte."INGIdMonedaPagoCupon" = 'CLP' THEN SUM(Carte."INGMontoCorteCupon" * -1) ELSE SUM(Carte."INGMontoCorteCuponMonedaInstrumento" * -1) END AS "Total"
		,Carte."INGIdMonedaPagoCupon"									AS "IdMoneda"
		,GTA."INGNumeroCupon"											AS "SecuenciaCupon"	
		,TO_BIGINT(0)													AS "IdEmpleadoEjecutivoPortafolio"
		,''																AS "CodigoUsuarioAgente"	
		,''																AS "NombreAgente"
		,''																AS "CodigoSucursal"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraIngresoRegistro"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraUltimaActualizacion"			
		,CASE IFNULL(DocIde."DocumentoIdentidad",'')
			WHEN '' THEN 'PEN'
			ELSE 'VIG'
			END															AS "IdEstado"
		,Carte."IdBoveda"												AS "IdBoveda"
		,Carte."IdBolsa"												AS "IdBolsa"
		,''																AS "IdConceptoGarantia"
		,''																AS "IdTipoOperacionGarantia"
		,SUM(Carte."INGMontoReajusteCorteCupon")						AS "MontoReajuste"
		,'SIS'															AS "Origen"
		,''																AS "IdTipoReajuste"
		,TO_DECIMAL(0,26,8)												AS "INGMontoInteresCorteCupon"
		,''	AS "TipoOperacion"
FROM  "BSCL.Custodia.BD::HIS.Garantia" 											AS GTA
INNER JOIN	"BSCL.Custodia.BD::HIS.Cartera"										AS Carte ON (Carte."FolioCartera" = GTA."FolioCarteraOrigen" AND Carte."Nemotecnico" = GTA."Nemotecnico")
LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 				AS Instru ON Carte."IdInstrumento" = Instru."IdInstrumento"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad"	AS DocIde ON Instru."DocumentoIdentidadEmisor" = DocIde."DocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"		AS SocNeg ON DocIde."IdPersona" = SocNeg."IdPersona"
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 			EC
	ON 	(   Carte."IdInstrumento" 		 = EC."IdInstrumento" 			AND 
			Carte."IdTipoInstrumento" 	 = EC."IdTipoInstrumento"		AND
			EC."IdTipoEventoCapital"	 = 'CC'                         AND			
			Carte."INGFechaPagoCorteCupon" = EC."FechaLimite"           AND
			EC."IdEstado"               <> 'ANU'
		)

WHERE	Carte."IdClasificacionTipoInstrumento"	= 'RF'					
AND 	Carte."INGFechaPagoCorteCupon"			BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta	

AND 	GTA."IdEstado" = 'VIG'             -- La garantia debe estar Vigente
AND 	Carte."IdEstado" IN ('LIQ', 'RPA')  -- La cartera que genero la garantia debe haber quedado liquidada o retirada parcialmente

AND	    Carte."IdBoveda" 						= 'CIA'			
AND	    EC."IdTipoInstrumento" 					IS NULL
AND	  	Carte."INGFechaCorteCupon"				<> Carte."INGFechaPagoCorteCupon"
AND   	Carte."FechaRespaldo"					= Carte."INGFechaCorteCupon" 		

AND     Carte."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN Carte."IdInstrumento" ELSE :IdInstrumento END)


AND     (CASE -- Letras hipotecarias cuyo nemo tiene "*" o "&" no generan pago cupon sino cambio de nemo
			WHEN Carte."IdTipoInstrumento" = 'LTH' AND LOCATE(Carte."Nemotecnico", '*') > 0 THEN 'CAMBIO_NEMO' 
			WHEN Carte."IdTipoInstrumento" = 'LTH' AND LOCATE(Carte."Nemotecnico", '&') > 0 THEN 'CAMBIO_NEMO'
			ELSE 'CORTE_CUPON'
		END) = 'CORTE_CUPON' 	
	
	
GROUP BY 
	DocIde."DocumentoIdentidad",
	Instru."RazonSocial",
	Carte."IdClasificacionTipoInstrumento",	
	Carte."IdTipoInstrumento",		
	Carte."IdInstrumento",
	Carte."Nemotecnico",
	Carte."INGFechaPagoCorteCupon",
	Carte."INGIdMonedaPagoCupon",						
	GTA."INGNumeroCupon",					
	Carte."IdBoveda",
	Carte."IdBolsa",
	Carte."IdEstado"
UNION ALL
--Cartera Negativa
SELECT 
		'CC'															AS "IdTipoEventoCapital"
		,Carte."NumeroCupon"											AS "Secuencia"	
		,TO_BIGINT(0)													AS "IdSocioNegocio"
		,'CNT'			   												AS "TipoSocioNegocio"
		,TO_BIGINT(0)													AS "IdPortafolio"
		,'R'															AS "IdTipoDocumentoIdentidad"
		,IFNULL(DocIde."DocumentoIdentidad",'')							AS "DocumentoIdentidad"
		,CASE IFNULL(DocIde."DocumentoIdentidad",'')
			WHEN '' THEN 'NO EXISTE SOCIO NEGOCIO CONTRAPARTE: '||Instru."RazonSocial"
			ELSE IFNULL(Instru."RazonSocial",'')
		END																AS "NombreCliente"
		,Instru."IdClasificacionTipoInstrumento"						AS "IdClasificacionTipoInstrumento"
		,Instru."IdTipoInstrumento"										AS "IdTipoInstrumento"
		,Instru."IdInstrumento"											AS "IdInstrumento"
		,Carte."Nemotecnico"											AS "Nemo"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,''																AS "NemoNuevo"
		,Carte."FechaPagoCorteCupon"									AS "FechaCierre"
		,Carte."FechaPagoCorteCupon"									AS "FechaPago"		
		,SUM("Cantidad")												AS "CantidadDerecho"
		,'CUS'															AS "TipoCartera"
		,TO_DECIMAL(0,26,8)												AS "CantidadNueva"
		,SUM(Carte."MontoCapitalCorteCupon")							AS "MontoCapital"	
		,SUM(Carte."MontoInteresCorteCupon")							AS "MontoInteres"
		,SUM(Carte."MontoCorteCupon")									AS "Total"
		,Carte."IdMonedaPagoCupon"										AS "IdMoneda"
		,Carte."NumeroCupon"											AS "SecuenciaCupon"	
		,TO_BIGINT(0)													AS "IdEmpleadoEjecutivoPortafolio"
		,''																AS "CodigoUsuarioAgente"	
		,''																AS "NombreAgente"
		,''																AS "CodigoSucursal"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraIngresoRegistro"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraUltimaActualizacion"			
		,CASE IFNULL(DocIde."DocumentoIdentidad",'')
			WHEN '' THEN 'PEN'
			ELSE 'VIG'
			END															AS "IdEstado"
		,Carte."IdBoveda"												AS "IdBoveda"
		,Carte."IdBolsa"												AS "IdBolsa"
		,''																AS "IdConceptoGarantia"
		,''																AS "IdTipoOperacionGarantia"
		,SUM(Carte."MontoReajusteCorteCupon")*-1						AS "MontoReajuste"
		,'SIS'															AS "Origen"
		,''																AS "IdTipoReajuste"
		,TO_DECIMAL(0,26,8)												AS "INGMontoInteresCorteCupon"
		,''	AS "TipoOperacion"
FROM "BSCL.Custodia.BD::CUS.CarteraNegativa"									AS Carte
LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 				AS Instru ON Carte."IdInstrumento" = Instru."IdInstrumento"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad"	AS DocIde ON Instru."DocumentoIdentidadEmisor" = DocIde."DocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"		AS SocNeg ON DocIde."IdPersona" = SocNeg."IdPersona"
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 			EC
	ON 	(   Carte."IdInstrumento" 		 = EC."IdInstrumento" 			AND 
			Instru."IdTipoInstrumento" 	 = EC."IdTipoInstrumento"		AND
			EC."IdTipoEventoCapital"	 = 'CC'                         AND			
			Carte."FechaCorteCupon" = EC."FechaLimite"           AND
			EC."IdEstado"               <> 'ANU'
		)

WHERE	Instru."IdClasificacionTipoInstrumento"	= 'RF'					
AND 	Carte."FechaCorteCupon"					BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta	
AND	    Carte."IdEstado" 						IN('VIG','VCD')					
AND	    Carte."IdBoveda" 						= 'CIA'			
AND	    EC."IdTipoInstrumento" 					IS NULL
AND	  	Carte."FechaCorteCupon"				= Carte."FechaPagoCorteCupon"		

AND     Carte."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN Carte."IdInstrumento" ELSE :IdInstrumento END)


AND     (CASE -- Letras hipotecarias cuyo nemo tiene "*" o "&" no generan pago cupon sino cambio de nemo
			WHEN Instru."IdTipoInstrumento" = 'LTH' AND LOCATE(Instru."Nemotecnico", '*') > 0 THEN 'CAMBIO_NEMO' 
			WHEN Instru."IdTipoInstrumento" = 'LTH' AND LOCATE(Instru."Nemotecnico", '&') > 0 THEN 'CAMBIO_NEMO'
			ELSE 'CORTE_CUPON'
		END) = 'CORTE_CUPON' 	
	
	
GROUP BY 
	DocIde."DocumentoIdentidad",
	Instru."RazonSocial",
	Instru."IdClasificacionTipoInstrumento",	
	Instru."IdTipoInstrumento",		
	Instru."IdInstrumento",
	Carte."Nemotecnico",
	Carte."FechaPagoCorteCupon",
	Carte."IdMonedaPagoCupon",						
	Carte."NumeroCupon",					
	Carte."IdBoveda",
	Carte."IdBolsa",
	Carte."IdEstado"	

	UNION ALL
--Cartera Negativa Historica
SELECT 
		'CC'															AS "IdTipoEventoCapital"
		,Carte."NumeroCupon"											AS "Secuencia"	
		,TO_BIGINT(0)													AS "IdSocioNegocio"
		,'CNT'			   												AS "TipoSocioNegocio"
		,TO_BIGINT(0)													AS "IdPortafolio"
		,'R'															AS "IdTipoDocumentoIdentidad"
		,IFNULL(DocIde."DocumentoIdentidad",'')							AS "DocumentoIdentidad"
		,CASE IFNULL(DocIde."DocumentoIdentidad",'')
			WHEN '' THEN 'NO EXISTE SOCIO NEGOCIO CONTRAPARTE: '||Instru."RazonSocial"
			ELSE IFNULL(Instru."RazonSocial",'')
		END																AS "NombreCliente"
		,Instru."IdClasificacionTipoInstrumento"						AS "IdClasificacionTipoInstrumento"
		,Instru."IdTipoInstrumento"										AS "IdTipoInstrumento"
		,Instru."IdInstrumento"											AS "IdInstrumento"
		,Carte."Nemotecnico"											AS "Nemo"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,''																AS "NemoNuevo"
		,Carte."FechaPagoCorteCupon"									AS "FechaCierre"
		,Carte."FechaPagoCorteCupon"									AS "FechaPago"		
		,SUM("Cantidad")												AS "CantidadDerecho"
		,'CUS'															AS "TipoCartera"
		,TO_DECIMAL(0,26,8)												AS "CantidadNueva"
		,SUM(Carte."MontoCapitalCorteCupon")							AS "MontoCapital"	
		,SUM(Carte."MontoInteresCorteCupon")							AS "MontoInteres"
		,SUM(Carte."MontoCorteCupon")									AS "Total"
		,Carte."IdMonedaPagoCupon"										AS "IdMoneda"
		,Carte."NumeroCupon"											AS "SecuenciaCupon"	
		,TO_BIGINT(0)													AS "IdEmpleadoEjecutivoPortafolio"
		,''																AS "CodigoUsuarioAgente"	
		,''																AS "NombreAgente"
		,''																AS "CodigoSucursal"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraIngresoRegistro"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraUltimaActualizacion"			
		,CASE IFNULL(DocIde."DocumentoIdentidad",'')
			WHEN '' THEN 'PEN'
			ELSE 'VIG'
			END															AS "IdEstado"
		,Carte."IdBoveda"												AS "IdBoveda"
		,Carte."IdBolsa"												AS "IdBolsa"
		,''																AS "IdConceptoGarantia"
		,''																AS "IdTipoOperacionGarantia"
		,SUM(Carte."MontoReajusteCorteCupon")*-1						AS "MontoReajuste"
		,'SIS'															AS "Origen"
		,''																AS "IdTipoReajuste"
		,TO_DECIMAL(0,26,8)												AS "INGMontoInteresCorteCupon"
		,''	AS "TipoOperacion"
FROM "BSCL.Custodia.BD::HIS.CarteraNegativa"									AS Carte
LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 				AS Instru ON Carte."IdInstrumento" = Instru."IdInstrumento"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad"	AS DocIde ON Instru."DocumentoIdentidadEmisor" = DocIde."DocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"		AS SocNeg ON DocIde."IdPersona" = SocNeg."IdPersona"
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 			EC
	ON 	(   Carte."IdInstrumento" 		 = EC."IdInstrumento" 			AND 
			Instru."IdTipoInstrumento" 	 = EC."IdTipoInstrumento"		AND
			EC."IdTipoEventoCapital"	 = 'CC'                         AND			
			Carte."FechaCorteCupon" = EC."FechaLimite"           AND
			EC."IdEstado"               <> 'ANU'
		)

WHERE	Instru."IdClasificacionTipoInstrumento"	= 'RF'					
AND 	Carte."FechaCorteCupon"					BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta	
AND	    Carte."IdEstado" 						IN('VIG','VCD')					
AND	    Carte."IdBoveda" 						= 'CIA'			
AND	    EC."IdTipoInstrumento" 					IS NULL
AND	  	Carte."FechaCorteCupon"				<> Carte."FechaPagoCorteCupon"
AND   	Carte."FechaRespaldo"					= Carte."FechaCorteCupon" 		

AND     Carte."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN Carte."IdInstrumento" ELSE :IdInstrumento END)


AND     (CASE -- Letras hipotecarias cuyo nemo tiene "*" o "&" no generan pago cupon sino cambio de nemo
			WHEN Instru."IdTipoInstrumento" = 'LTH' AND LOCATE(Instru."Nemotecnico", '*') > 0 THEN 'CAMBIO_NEMO' 
			WHEN Instru."IdTipoInstrumento" = 'LTH' AND LOCATE(Instru."Nemotecnico", '&') > 0 THEN 'CAMBIO_NEMO'
			ELSE 'CORTE_CUPON'
		END) = 'CORTE_CUPON' 	
	
	
GROUP BY 
	DocIde."DocumentoIdentidad",
	Instru."RazonSocial",
	Instru."IdClasificacionTipoInstrumento",	
	Instru."IdTipoInstrumento",		
	Instru."IdInstrumento",
	Carte."Nemotecnico",
	Carte."FechaPagoCorteCupon",
	Carte."IdMonedaPagoCupon",						
	Carte."NumeroCupon",					
	Carte."IdBoveda",
	Carte."IdBolsa",
	Carte."IdEstado";	