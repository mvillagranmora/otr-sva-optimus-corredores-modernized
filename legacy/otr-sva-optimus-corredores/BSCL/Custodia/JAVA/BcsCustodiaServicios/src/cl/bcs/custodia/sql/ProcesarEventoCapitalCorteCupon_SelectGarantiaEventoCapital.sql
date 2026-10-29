WITH ultimoRespaldo AS (
	SELECT 
		MAX(Garantia."FechaRespaldo") AS "FechaUltimoRespaldo"
	FROM 
		"BSCL.Custodia.BD::HIS.Garantia" AS Garantia
	WHERE 
		Garantia."FechaRespaldo" BETWEEN ADD_DAYS(:FechaCorteCuponDesde, -7) AND ADD_DAYS(:FechaCorteCuponDesde, -1)
), CuponesGarantiaHistorica AS (
	SELECT 
		CAR."FolioCarteraOrigen",
		CAR."IdGarantia",
		CAR."IdEstado", 
		MAX(CAR."FechaRespaldo") AS "MaxFechaRespaldo",
		CAR."Nemotecnico"

	FROM "BSCL.Custodia.BD::HIS.Garantia" 	 AS CAR
	INNER JOIN ultimoRespaldo UR ON  CAR."FechaRespaldo" = UR."FechaUltimoRespaldo"  AND CAR."IdEstado" = 'VIG'
	WHERE 
		CAR."IdClasificacionTipoInstrumento"   = 'RF'
		AND CAR."FechaRespaldo" BETWEEN ADD_DAYS(:FechaCorteCuponDesde, -7) AND ADD_DAYS(:FechaCorteCuponDesde, -1)
		AND   CAR."INGFechaPagoCorteCupon"			BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta	
		AND   CAR."FechaLiquidacion" 				<= :FechaCorteCuponDesde
		AND   CAR."IdEstado" 						= 'VIG'
		AND	  CAR."INGFechaCorteCupon"				<= CAR."INGFechaPagoCorteCupon"
		AND   CAR."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CAR."IdInstrumento" ELSE :IdInstrumento END)
		AND   CAR."FechaRespaldo"					 < CAR."INGFechaCorteCupon"
		GROUP BY 
		CAR."IdGarantia",
		CAR."FolioCarteraOrigen",
		CAR."IdEstado",
		CAR."Nemotecnico",
		CAR."INGNumeroCupon"
				
)SELECT 
		'CC'															AS "IdTipoEventoCapital"
		,'N'															AS "EsCarteraNegativa"
		,GTA."INGNumeroCupon"											AS "Secuencia"	
		,CASE 
		   WHEN MAX(CA."IdTipoOperacion") IN ('COMPAC','VENPAC') THEN IFNULL(MAX(SOCREL."IdSocioDeNegocio"), 0)
		 ELSE IFNULL(SOC."IdSocioDeNegocio",0)
		 END															AS "IdSocioNegocio"
		,CASE 
			WHEN SOC."IdCliente" > 0 		THEN 'CLI'
			WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
			WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
			WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
		END																AS "TipoSocioNegocio"
		,CASE 
		   WHEN MAX(CA."IdTipoOperacion") IN ('COMPAC','VENPAC') THEN MAX(CA."IdPortafolioRelacionado")
		 ELSE CA."IdPortafolio"
		 END 															AS "IdPortafolio"
		,'R'															AS "IdTipoDocumentoIdentidad"
		,CASE 
		   WHEN MAX(CA."IdTipoOperacion") IN ('COMPAC','VENPAC') THEN MAX(DOCIDEREL."DocumentoIdentidad")
		 ELSE CA."DocumentoIdentidad"
		 END 															AS "DocumentoIdentidad"
		,CASE
           WHEN MAX(CA."IdTipoOperacion") IN ('COMPAC','VENPAC') THEN CASE
           WHEN MAX(PERREL."Tipo") = 'N' THEN MAX(PERREL."Nombres") ||' '|| MAX(PERREL."ApellidoPaterno") || ' ' || MAX(PERREL."ApellidoMaterno")
           ELSE MAX(PERREL."RazonSocial")
        END 		                                                                                              
		 ELSE IFNULL(CL."NombreCompleto",'')
		 END 															AS "NombreCliente"
		,CA."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento"
		,CA."IdTipoInstrumento"											AS "IdTipoInstrumento"
		,CA."IdInstrumento"												AS "IdInstrumento"
		,CA."Nemotecnico"												AS "Nemo"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,''																AS "NemoNuevo"
		,GTA."INGFechaPagoCorteCupon"									AS "FechaCierre"
		,GTA."INGFechaPagoCorteCupon"									AS "FechaPago"
		,SUM(GTA."INGCantidad")											AS "CantidadDerecho"
		,'GAR'															AS "TipoCartera"
		,TO_DECIMAL(0,26,8)												AS "CantidadNueva"

		--AMORTIZACION
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND GTA."INGFechaPagoCorteCupon" != GTA."INGFechaCorteCupon"
			THEN CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP' 
					THEN SUM(GTA."INGValorFactorMonedaInstrumentoProximo" * (GTA."INGMontoCapitalCorteCupon"/GTA."INGValorFactorMonedaInstrumentoActual"))
					ELSE SUM(GTA."INGMontoCapitalCorteCuponMonedaInstrumento") 
				 END
		ELSE 
				CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP'
					THEN SUM(GTA."INGMontoCapitalCorteCupon") 
					ELSE SUM(GTA."INGMontoCapitalCorteCuponMonedaInstrumento") 
				END
		END	AS "MontoCapital" --AMORTIZACION
		--INTERES
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND GTA."INGFechaPagoCorteCupon" != GTA."INGFechaCorteCupon"
			THEN CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP' 
				THEN SUM(GTA."INGValorFactorMonedaInstrumentoProximo" * (GTA."INGMontoInteresCorteCupon"/GTA."INGValorFactorMonedaInstrumentoActual"))
				ELSE SUM(GTA."INGMontoInteresCorteCuponMonedaInstrumento") 
			END
		ELSE 
			CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP' 
				THEN SUM(GTA."INGMontoInteresCorteCupon") 
				ELSE SUM(GTA."INGMontoInteresCorteCuponMonedaInstrumento") 
			END
		END	AS "MontoInteres" --INTERES
		--TOTAL
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND GTA."INGFechaPagoCorteCupon" != GTA."INGFechaCorteCupon"
			THEN CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP'
				THEN SUM(GTA."INGValorFactorMonedaInstrumentoProximo" * (GTA."INGMontoCorteCupon"/GTA."INGValorFactorMonedaInstrumentoActual")) 
				ELSE SUM(GTA."INGMontoCorteCuponMonedaInstrumento") 
			END
		 ELSE
		 	CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP'
		 		THEN SUM(GTA."INGMontoCorteCupon") 
		 		ELSE SUM(GTA."INGMontoCorteCuponMonedaInstrumento") 
		 	END
		END	AS "Total"
		,GTA."INGIdMonedaPagoCupon"										AS "IdMoneda"
		,GTA."INGNumeroCupon"											AS "SecuenciaCupon"			
		,COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0)				AS "IdEmpleadoEjecutivoPortafolio"
		,COALESCE(EJE."CodigoEmpleado",'')								AS "CodigoUsuarioAgente"
		,COALESCE(EJE."NombreCompletoEjecutivo",'')						AS "NombreAgente"
		,COALESCE(EJE."CodigoSucursal",'')								AS "CodigoSucursal"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraIngresoRegistro"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraUltimaActualizacion"		
		,'VIG'															AS "IdEstado"
		,CA."IdBoveda"													AS "IdBoveda"
		,CA."IdBolsa"													AS "IdBolsa"
		,''																AS "IdConceptoGarantia"
		,''																AS "IdTipoOperacionGarantia"
		,SUM(CA."INGMontoReajusteCorteCupon")							AS "MontoReajuste"
		,'SIS'															AS "Origen"
		,CA."IdTipoReajuste"											AS "IdTipoReajuste"
		,GTA."INGMontoInteresCorteCupon"								AS "INGMontoInteresCorteCupon"		
		,COALESCE(CA."IdTipoOperacion",'') AS "IdTipoOperacion"
		,''	AS "TipoOperacion"
		,GTA."FechaLiquidacion"
		,GTA."INGValorFactorMonedaInstrumentoProximo"
		,GTA."INGFechaCorteCupon"
		,GTA."INGFechaFactorMonedaAdicionalProximo"
		,GTA."INGValorFactorMonedaInstrumentoActual"
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND GTA."INGFechaPagoCorteCupon" != GTA."INGFechaCorteCupon" THEN 'S' 
			ELSE 'N' END AS "UsaProximoFactorValorMoneda"
		,'' AS "INGIdTipoOperacion"
		,CA."IdCartera"
		,'GARANTIA' as "QueryOrigen"
FROM       "BSCL.Custodia.BD::GTA.Garantia" AS GTA
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CA ON ( CA."Nemotecnico" = GTA."Nemotecnico" AND GTA."IdCarteraOrigen" = CA."IdCartera")
LEFT JOIN  "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON (SOC."IdCliente" = CA."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"				AS CL   ON (CA."IdCliente" = CL."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"                               AS CLREL ON CLREL."IdCliente" = CA."IdClienteRelacionado"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"                               AS PERREL ON PERREL."IdPersona" = CLREL."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"                    AS DOCIDEREL ON DOCIDEREL."IdDocumentoIdentidad" = PERREL."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"	AS EJE  ON ((CA."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CA."IdPortafolioRelacionado" = EJE."IdPortafolio") OR (CA."IdTipoOperacion" NOT IN ('VENPAC','COMPAC') AND CA."IdPortafolio" = EJE."IdPortafolio"))
LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"   AS SOCREL ON (CA."IdClienteRelacionado" = SOCREL."IdCliente")
LEFT JOIN  "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = SOC."IdPersona")
LEFT JOIN  "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON (DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
LEFT JOIN  "BSCL.Portafolio.BD::POR.Portafolio" POR	ON (POR."IdPortafolio" = CA."IdPortafolio")
INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" 								AS EMP  ON EMP."IdEmpresa" = CA."IdEmpresa"
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 						AS SOCCLIREL ON SOCCLIREL."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"

WHERE 	CA."IdClasificacionTipoInstrumento" = 'RF'
AND		GTA."INGFechaPagoCorteCupon" BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta
AND     GTA."FechaLiquidacion" 		      <= :FechaCorteCuponDesde
AND     GTA."INGFechaOperacion"         < GTA."INGFechaCorteCupon"
AND     CA."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CA."IdInstrumento" ELSE :IdInstrumento END)
AND 	GTA."IdEstado" = 'VIG'             -- La garantia debe estar Vigente
AND 	CA."IdEstado" IN ('LIQ', 'RPA')  -- La cartera que genero la garantia debe haber quedado liquidada o retirada parcialmente


GROUP BY 
	CA."IdCartera",
	IFNULL(SOC."IdSocioDeNegocio",0),
	CASE 
		WHEN SOC."IdCliente" > 0 		THEN 'CLI'
		WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
		WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
		WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
	END,
	CA."IdPortafolio",				
	CA."DocumentoIdentidad",
	IFNULL(CL."NombreCompleto",''),				
	CA."IdClasificacionTipoInstrumento",	
	CA."IdTipoInstrumento",		
	CA."IdInstrumento",
	CA."Nemotecnico",
	GTA."INGIdMonedaPagoCupon",						
	GTA."INGNumeroCupon",
	COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0),
	COALESCE(EJE."CodigoEmpleado",''),
	COALESCE(EJE."NombreCompletoEjecutivo",''),
	COALESCE(EJE."CodigoSucursal",''),			
	CA."IdBoveda",
	CA."IdBolsa",
	CA."IdTipoReajuste",
	GTA."INGMontoInteresCorteCupon",	
	CA."IdTipoOperacion",
	CASE 
	  WHEN :AgruparEventoCapitalCliente = 'N' THEN CA."INGMontoCapitalCorteCuponMonedaInstrumento"
	END,
	CASE 
	  WHEN :AgruparEventoCapitalCliente = 'N' THEN CA."INGMontoCorteCuponMonedaInstrumento"
	END,
	GTA."FechaLiquidacion",
	GTA."INGValorFactorMonedaInstrumentoProximo",
	GTA."INGFechaCorteCupon",
	GTA."INGFechaPagoCorteCupon",
	GTA."INGFechaFactorMonedaAdicionalProximo",
	GTA."INGValorFactorMonedaInstrumentoActual"
	
	UNION ALL
	--Garantia historica
	SELECT 
		'CC'															AS "IdTipoEventoCapital"
		,'N'															AS "EsCarteraNegativa"
		,GTA."INGNumeroCupon"											AS "Secuencia"	
		,CASE 
		   WHEN MAX(CA."IdTipoOperacion") IN ('COMPAC','VENPAC') THEN IFNULL(MAX(SOCREL."IdSocioDeNegocio"), 0)
		 ELSE IFNULL(SOC."IdSocioDeNegocio",0)
		 END															AS "IdSocioNegocio"
		,CASE 
			WHEN SOC."IdCliente" > 0 		THEN 'CLI'
			WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
			WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
			WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
		END																AS "TipoSocioNegocio"
		,CASE 
		   WHEN MAX(CA."IdTipoOperacion") IN ('COMPAC','VENPAC') THEN MAX(CA."IdPortafolioRelacionado")
		 ELSE CA."IdPortafolio"
		 END 															AS "IdPortafolio"
		,'R'															AS "IdTipoDocumentoIdentidad"
		,CASE 
		   WHEN MAX(CA."IdTipoOperacion") IN ('COMPAC','VENPAC') THEN MAX(DOCIDEREL."DocumentoIdentidad")
		 ELSE CL."DocumentoIdentidad"
		 END 															AS "DocumentoIdentidad"
		,CASE
           WHEN MAX(CA."IdTipoOperacion") IN ('COMPAC','VENPAC') THEN CASE
           WHEN MAX(PERREL."Tipo") = 'N' THEN MAX(PERREL."Nombres") ||' '|| MAX(PERREL."ApellidoPaterno") || ' ' || MAX(PERREL."ApellidoMaterno")
           ELSE MAX(PERREL."RazonSocial")
        END 		                                                                                              
		 ELSE IFNULL(CL."NombreCompleto",'')
		 END 															AS "NombreCliente"
		,CA."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento"
		,CA."IdTipoInstrumento"											AS "IdTipoInstrumento"
		,CA."IdInstrumento"												AS "IdInstrumento"
		,CA."Nemotecnico"												AS "Nemo"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,''																AS "NemoNuevo"
		,GTA."INGFechaPagoCorteCupon"									AS "FechaCierre"
		,GTA."INGFechaPagoCorteCupon"									AS "FechaPago"
		,GTA."INGCantidad"										AS "CantidadDerecho"
		,'GAR'															AS "TipoCartera"
		,TO_DECIMAL(0,26,8)												AS "CantidadNueva"
		--AMORTIZACION
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND GTA."INGFechaPagoCorteCupon" != GTA."INGFechaCorteCupon"
			THEN CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP' 
					THEN (GTA."INGValorFactorMonedaInstrumentoProximo" * (GTA."INGMontoCapitalCorteCupon"/GTA."INGValorFactorMonedaInstrumentoActual"))
					ELSE (GTA."INGMontoCapitalCorteCuponMonedaInstrumento") 
				 END
		ELSE 
				CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP'
					THEN (GTA."INGMontoCapitalCorteCupon") 
					ELSE (GTA."INGMontoCapitalCorteCuponMonedaInstrumento") 
				END
		END	AS "MontoCapital" --AMORTIZACION
		--INTERES
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND GTA."INGFechaPagoCorteCupon" != GTA."INGFechaCorteCupon"
			THEN CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP' 
				THEN (GTA."INGValorFactorMonedaInstrumentoProximo" * (GTA."INGMontoInteresCorteCupon"/GTA."INGValorFactorMonedaInstrumentoActual"))
				ELSE (GTA."INGMontoInteresCorteCuponMonedaInstrumento") 
			END
		ELSE 
			CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP' 
				THEN (GTA."INGMontoInteresCorteCupon") 
				ELSE (GTA."INGMontoInteresCorteCuponMonedaInstrumento") 
			END
		END	AS "MontoInteres" --INTERES
		--TOTAL
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND GTA."INGFechaPagoCorteCupon" != GTA."INGFechaCorteCupon"
			THEN CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP'
				THEN (GTA."INGValorFactorMonedaInstrumentoProximo" * (GTA."INGMontoCorteCupon"/GTA."INGValorFactorMonedaInstrumentoActual")) 
				ELSE (GTA."INGMontoCorteCuponMonedaInstrumento") 
			END
		 ELSE
		 	CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP'
		 		THEN (GTA."INGMontoCorteCupon") 
		 		ELSE (GTA."INGMontoCorteCuponMonedaInstrumento") 
		 	END
		END	AS "Total"
		,GTA."INGIdMonedaPagoCupon"										AS "IdMoneda"
		,GTA."INGNumeroCupon"											AS "SecuenciaCupon"			
		,COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0)				AS "IdEmpleadoEjecutivoPortafolio"
		,COALESCE(EJE."CodigoEmpleado",'')								AS "CodigoUsuarioAgente"
		,COALESCE(EJE."NombreCompletoEjecutivo",'')						AS "NombreAgente"
		,COALESCE(EJE."CodigoSucursal",'')								AS "CodigoSucursal"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraIngresoRegistro"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraUltimaActualizacion"		
		,'VIG'															AS "IdEstado"
		,CA."IdBoveda"													AS "IdBoveda"
		,CA."IdBolsa"													AS "IdBolsa"
		,''																AS "IdConceptoGarantia"
		,''																AS "IdTipoOperacionGarantia"
		,SUM(CA."INGMontoReajusteCorteCupon")							AS "MontoReajuste"
		,'SIS'															AS "Origen"
		,CA."IdTipoReajuste"											AS "IdTipoReajuste"
		,GTA."INGMontoInteresCorteCupon"									AS "INGMontoInteresCorteCupon"		
		,COALESCE(CA."IdTipoOperacion",'') AS "IdTipoOperacion"
		,''	AS "TipoOperacion"
		,GTA."FechaLiquidacion"
		,GTA."INGValorFactorMonedaInstrumentoProximo"
		,GTA."INGFechaCorteCupon"
		,GTA."INGFechaFactorMonedaAdicionalProximo"
		,GTA."INGValorFactorMonedaInstrumentoActual"
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND GTA."INGFechaPagoCorteCupon" != GTA."INGFechaCorteCupon" THEN 'S' 
			ELSE 'N' END AS "UsaProximoFactorValorMoneda"
		,'' AS "INGIdTipoOperacion"
		,CA."IdCartera"
		,'GARANTIA_HISTORICA' as "QueryOrigen"
FROM       "BSCL.Custodia.BD::HIS.Garantia" AS GTA
INNER JOIN CuponesGarantiaHistorica AS HIS ON (HIS."FolioCarteraOrigen" = GTA."FolioCarteraOrigen" AND HIS."Nemotecnico" = GTA."Nemotecnico" AND HIS."MaxFechaRespaldo" = GTA."FechaRespaldo" )
INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" AS CA ON (CA."Nemotecnico" = GTA."Nemotecnico" AND GTA."IdCarteraOrigen" = CA."IdCartera" AND CA."FechaRespaldo" = GTA."FechaRespaldo")
LEFT JOIN  "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON (SOC."IdCliente" = CA."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"				AS CL   ON (CA."IdCliente" = CL."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"                               AS CLREL ON CLREL."IdCliente" = CA."IdClienteRelacionado"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"                               AS PERREL ON PERREL."IdPersona" = CLREL."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"                    AS DOCIDEREL ON DOCIDEREL."IdDocumentoIdentidad" = PERREL."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"	AS EJE  ON ((CA."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CA."IdPortafolioRelacionado" = EJE."IdPortafolio") OR (CA."IdTipoOperacion" NOT IN ('VENPAC','COMPAC') AND CA."IdPortafolio" = EJE."IdPortafolio"))
LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"   AS SOCREL ON (CA."IdClienteRelacionado" = SOCREL."IdCliente")
LEFT JOIN  "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = SOC."IdPersona")
LEFT JOIN  "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON (DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
LEFT JOIN  "BSCL.Portafolio.BD::POR.Portafolio" POR	ON (POR."IdPortafolio" = CA."IdPortafolio")
INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" 								AS EMP  ON EMP."IdEmpresa" = CA."IdEmpresa"
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 						AS SOCCLIREL ON SOCCLIREL."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 			EC
ON 	(   CA."IdInstrumento" 		  = EC."IdInstrumento" 			AND 
		CA."IdTipoInstrumento" 	  = EC."IdTipoInstrumento"		AND
		GTA."INGFechaPagoCorteCupon" = EC."FechaPago"          	AND
		EC."IdTipoEventoCapital"  = 'CC'                        AND   
		EC."IdEstado"            <> 'ANU'
	)


WHERE 	CA."IdClasificacionTipoInstrumento" = 'RF'
AND		GTA."INGFechaPagoCorteCupon" BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta
AND     GTA."FechaLiquidacion" 		      <= :FechaCorteCuponDesde
AND     CA."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CA."IdInstrumento" ELSE :IdInstrumento END)
AND 	GTA."IdEstado" = 'VIG'             -- La garantia debe estar Vigente
AND 	CA."IdEstado" IN ('LIQ', 'RPA')  -- La cartera que genero la garantia debe haber quedado liquidada o retirada parcialmente
AND	  	"MaxFechaRespaldo"				< GTA."INGFechaPagoCorteCupon"


GROUP BY 
	CA."IdCartera",
	IFNULL(SOC."IdSocioDeNegocio",0),
	CASE 
		WHEN SOC."IdCliente" > 0 		THEN 'CLI'
		WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
		WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
		WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
	END,
	CA."IdPortafolio",				
	CL."DocumentoIdentidad",
	IFNULL(CL."NombreCompleto",''),				
	CA."IdClasificacionTipoInstrumento",	
	CA."IdTipoInstrumento",		
	CA."IdInstrumento",
	CA."Nemotecnico",
	GTA."INGCantidad",
	GTA."INGIdMonedaPagoCupon",						
	GTA."INGNumeroCupon",
	COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0),
	COALESCE(EJE."CodigoEmpleado",''),
	COALESCE(EJE."NombreCompletoEjecutivo",''),
	COALESCE(EJE."CodigoSucursal",''),			
	CA."IdBoveda",
	CA."IdBolsa",
	CA."IdTipoReajuste",
	GTA."INGMontoInteresCorteCupon"	,
	CA."IdTipoOperacion",
	CASE 
	  WHEN :AgruparEventoCapitalCliente = 'N' THEN CA."INGMontoCapitalCorteCuponMonedaInstrumento"
	END,
	CASE 
	  WHEN :AgruparEventoCapitalCliente = 'N' THEN CA."INGMontoCorteCuponMonedaInstrumento"
	END,
	GTA."FechaLiquidacion",
	GTA."INGMontoCorteCupon",
	GTA."INGMontoCorteCuponMonedaInstrumento",
	GTA."INGValorFactorMonedaInstrumentoProximo",
	GTA."INGMontoCapitalCorteCuponMonedaInstrumento",
	GTA."INGFechaCorteCupon",
	GTA."INGFechaPagoCorteCupon",
	GTA."INGMontoCapitalCorteCupon",
	GTA."INGFechaFactorMonedaAdicionalProximo",
	GTA."INGValorFactorMonedaInstrumentoActual",
	GTA."INGMontoInteresCorteCuponMonedaInstrumento"