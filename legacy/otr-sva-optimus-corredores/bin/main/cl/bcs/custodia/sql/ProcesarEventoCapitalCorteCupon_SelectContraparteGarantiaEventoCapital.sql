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
		AND   CAR."FechaRespaldo" BETWEEN ADD_DAYS(:FechaCorteCuponDesde, -7) AND ADD_DAYS(:FechaCorteCuponDesde, -1)
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
)
SELECT
		'CC'															AS "IdTipoEventoCapital"
		,'N'															AS "EsCarteraNegativa"
		,GTA."INGNumeroCupon"											AS "Secuencia"	
		,TO_BIGINT(0)													AS "IdSocioNegocio"
		,'CNT'			   												AS "TipoSocioNegocio"
		,TO_BIGINT(0)													AS "IdPortafolio"
		,'R'															AS "IdTipoDocumentoIdentidad"
		,COALESCE(DI."DocumentoIdentidad",'')                           AS "DocumentoIdentidad"
		,COALESCE(DI."NombreCompleto",PE."RazonSocial",'')              AS "NombreCliente"
		,CA."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento"
		,CA."IdTipoInstrumento"											AS "IdTipoInstrumento"
		,CA."IdInstrumento"												AS "IdInstrumento"
		,CA."Nemotecnico"												AS "Nemo"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,''																AS "NemoNuevo"
		,GTA."INGFechaPagoCorteCupon"									AS "FechaCierre"
		,GTA."INGFechaPagoCorteCupon"									AS "FechaPago"
		,SUM(GTA."INGCantidad") * -1									AS "CantidadDerecho"
		,'GAR'															AS "TipoCartera"
		,TO_DECIMAL(0,26,8)												AS "CantidadNueva"
	
		--AMORTIZACION
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND GTA."INGFechaPagoCorteCupon" != GTA."INGFechaCorteCupon"
			THEN CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP' 
					THEN SUM(GTA."INGValorFactorMonedaInstrumentoProximo" * (GTA."INGMontoCapitalCorteCupon"/GTA."INGValorFactorMonedaInstrumentoActual")) * -1
					ELSE SUM(GTA."INGMontoCapitalCorteCuponMonedaInstrumento") * -1
				 END
		ELSE 
				CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP'
					THEN SUM(GTA."INGMontoCapitalCorteCupon") * -1
					ELSE SUM(GTA."INGMontoCapitalCorteCuponMonedaInstrumento") * -1
				END
		END	AS "MontoCapital" --AMORTIZACION
		--INTERES
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND GTA."INGFechaPagoCorteCupon" != GTA."INGFechaCorteCupon"
			THEN CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP' 
				THEN SUM(GTA."INGValorFactorMonedaInstrumentoProximo" * (GTA."INGMontoInteresCorteCupon"/GTA."INGValorFactorMonedaInstrumentoActual")) * -1
				ELSE SUM(GTA."INGMontoInteresCorteCuponMonedaInstrumento") * -1
			END
		ELSE 
			CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP' 
				THEN SUM(GTA."INGMontoInteresCorteCupon") * -1
				ELSE SUM(GTA."INGMontoInteresCorteCuponMonedaInstrumento") * -1
			END
		END	AS "MontoInteres" --INTERES
		--TOTAL
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND GTA."INGFechaPagoCorteCupon" != GTA."INGFechaCorteCupon"
			THEN CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP'
				THEN SUM(GTA."INGValorFactorMonedaInstrumentoProximo" * (GTA."INGMontoCorteCupon"/GTA."INGValorFactorMonedaInstrumentoActual")) * -1
				ELSE SUM(GTA."INGMontoCorteCuponMonedaInstrumento") * -1
			END
		 ELSE
		 	CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP'
		 		THEN SUM(GTA."INGMontoCorteCupon") * -1
		 		ELSE SUM(GTA."INGMontoCorteCuponMonedaInstrumento") * -1
		 	END
		END	AS "Total"
		,GTA."INGIdMonedaPagoCupon"										AS "IdMoneda"
		,GTA."INGNumeroCupon"											AS "SecuenciaCupon"	
		,TO_BIGINT(0)													AS "IdEmpleadoEjecutivoPortafolio"
		,''																AS "CodigoUsuarioAgente"	
		,''																AS "NombreAgente"
		,''																AS "CodigoSucursal"
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
		,TO_DECIMAL(0,26,8)												AS "INGMontoInteresCorteCupon"  
		,''	AS "TipoOperacion"
		,GTA."FechaLiquidacion"
		,GTA."INGValorFactorMonedaInstrumentoProximo"
		,GTA."INGFechaCorteCupon"
		,COALESCE(CA."IdTipoOperacion",'') AS "IdTipoOperacion"
		,GTA."INGFechaFactorMonedaAdicionalProximo"
		,GTA."INGValorFactorMonedaInstrumentoActual"
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND GTA."INGFechaPagoCorteCupon" != GTA."INGFechaCorteCupon" THEN 'S' 
			ELSE 'N' END AS "UsaProximoFactorValorMoneda"
		,'' AS "INGIdTipoOperacion"
		,CA."IdCartera"
		,'GARANTIA' as "QueryOrigen"
FROM  "BSCL.Custodia.BD::GTA.Garantia" 									AS GTA
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" 				AS CA ON (CA."FolioCartera" = GTA."FolioCarteraOrigen" AND CA."Nemotecnico" = GTA."Nemotecnico" AND GTA."IdCarteraOrigen" = CA."IdCartera")
INNER JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte"						AS CT ON (CT."IdContraparte" = GTA."IdContraparteGarantia")
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"							AS PE ON (PE."IdPersona" = CT."IdPersona")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"			AS DI ON (DI."IdPersona" = PE."IdPersona")
LEFT  JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 						AS EC
ON 	(CA."IdInstrumento" 		 = EC."IdInstrumento" 			AND 
	 CA."IdTipoInstrumento" 	 = EC."IdTipoInstrumento"		AND
	 EC."IdTipoEventoCapital"	 = 'CC'                         AND
	 CA."INGFechaPagoCorteCupon"   = EC."FechaPago"   	        AND
	 EC."IdEstado"	            <> 'ANU'
	)
WHERE CA."IdClasificacionTipoInstrumento" = 'RF'				
AND   GTA."INGFechaPagoCorteCupon"			BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta	
AND   GTA."FechaLiquidacion" 				<= :FechaCorteCuponDesde
AND   GTA."INGFechaOperacion"                 < :FechaCorteCuponDesde
AND   CA."IdBoveda" 					   <> 'CIA'				
AND   EC."IdTipoInstrumento" 				IS NULL	
AND     CA."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CA."IdInstrumento" ELSE :IdInstrumento END)
AND 	GTA."IdEstado" = 'VIG'             -- La garantia debe estar Vigente
AND 	CA."IdEstado" IN ('LIQ', 'RPA')  -- La cartera que genero la garantia debe haber quedado liquidada o retirada parcialmente

GROUP BY 
	CA."IdCartera",
	DI."DocumentoIdentidad",
	DI."NombreCompleto",
	PE."RazonSocial",
	CA."IdClasificacionTipoInstrumento",	
	CA."IdTipoInstrumento",		
	CA."IdInstrumento",
	CA."Nemotecnico",
	CASE 
	  WHEN :AgruparEventoCapitalCliente = 'N' THEN CA."INGMontoCapitalCorteCuponMonedaInstrumento"
	END,
	CASE 
	  WHEN :AgruparEventoCapitalCliente = 'N' THEN CA."INGMontoCorteCuponMonedaInstrumento"
	END,
	GTA."IdPortafolio",
    GTA."INGFechaPagoCorteCupon",
	GTA."INGIdMonedaPagoCupon",
	GTA."INGNumeroCupon",
	CA."IdBoveda",					
	CA."IdBolsa",
	CA."IdTipoReajuste",
	CA."IdEstado",
	GTA."FechaLiquidacion",
	GTA."INGValorFactorMonedaInstrumentoProximo",
	GTA."INGFechaCorteCupon",
	CA."IdTipoOperacion",
	GTA."INGFechaFactorMonedaAdicionalProximo",
	GTA."INGValorFactorMonedaInstrumentoActual"

	UNION ALL
--Garantia Historica
 SELECT
		'CC'															AS "IdTipoEventoCapital"
		,'N'															AS "EsCarteraNegativa"
		,GTA."INGNumeroCupon"											AS "Secuencia"	
		,TO_BIGINT(0)													AS "IdSocioNegocio"
		,'CNT'			   												AS "TipoSocioNegocio"
		,TO_BIGINT(0) 												    AS "IdPortafolio"
		,'R'															AS "IdTipoDocumentoIdentidad"
		,COALESCE(DI."DocumentoIdentidad",'')                           AS "DocumentoIdentidad"
		,COALESCE(DI."NombreCompleto", PE."RazonSocial",'')             AS "NombreCliente"
		,CA."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento"
		,CA."IdTipoInstrumento"											AS "IdTipoInstrumento"
		,CA."IdInstrumento"												AS "IdInstrumento"
		,CA."Nemotecnico"												AS "Nemo"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,''																AS "NemoNuevo"
		,GTA."INGFechaPagoCorteCupon"									AS "FechaCierre"
		,GTA."INGFechaPagoCorteCupon"									AS "FechaPago"
		,(GTA."INGCantidad") * -1									AS "CantidadDerecho"
		,'GAR'															AS "TipoCartera"
		,TO_DECIMAL(0,26,8)												AS "CantidadNueva"
	--AMORTIZACION
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND GTA."INGFechaPagoCorteCupon" != GTA."INGFechaCorteCupon"
			THEN CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP' 
					THEN (GTA."INGValorFactorMonedaInstrumentoProximo" * (GTA."INGMontoCapitalCorteCupon"/GTA."INGValorFactorMonedaInstrumentoActual"))* -1
					ELSE (GTA."INGMontoCapitalCorteCuponMonedaInstrumento") * -1
				 END
		ELSE 
				CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP'
					THEN (GTA."INGMontoCapitalCorteCupon") * -1
					ELSE (GTA."INGMontoCapitalCorteCuponMonedaInstrumento") * -1
				END
		END	AS "MontoCapital" --AMORTIZACION
		--INTERES
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND GTA."INGFechaPagoCorteCupon" != GTA."INGFechaCorteCupon"
			THEN CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP' 
				THEN (GTA."INGValorFactorMonedaInstrumentoProximo" * (GTA."INGMontoInteresCorteCupon"/GTA."INGValorFactorMonedaInstrumentoActual"))* -1
				ELSE (GTA."INGMontoInteresCorteCuponMonedaInstrumento") * -1
			END
		ELSE 
			CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP' 
				THEN (GTA."INGMontoInteresCorteCupon") * -1
				ELSE (GTA."INGMontoInteresCorteCuponMonedaInstrumento") * -1
			END
		END	AS "MontoInteres" --INTERES
		--TOTAL
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND GTA."INGFechaPagoCorteCupon" != GTA."INGFechaCorteCupon"
			THEN CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP'
				THEN (GTA."INGValorFactorMonedaInstrumentoProximo" * (GTA."INGMontoCorteCupon"/GTA."INGValorFactorMonedaInstrumentoActual")) * -1
				ELSE (GTA."INGMontoCorteCuponMonedaInstrumento") * -1
			END
		 ELSE
		 	CASE WHEN GTA."INGIdMonedaPagoCupon" = 'CLP'
		 		THEN (GTA."INGMontoCorteCupon") * -1
		 		ELSE (GTA."INGMontoCorteCuponMonedaInstrumento") * -1
		 	END
		END	AS "Total"
		,GTA."INGIdMonedaPagoCupon"									AS "IdMoneda"
		,GTA."INGNumeroCupon"											AS "SecuenciaCupon"	
		,TO_BIGINT(0)													AS "IdEmpleadoEjecutivoPortafolio"
		,''																AS "CodigoUsuarioAgente"	
		,''																AS "NombreAgente"
		,''																AS "CodigoSucursal"
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
		,''	AS "TipoOperacion"
		,GTA."FechaLiquidacion"
		,GTA."INGValorFactorMonedaInstrumentoProximo"
		,GTA."INGFechaCorteCupon"
		,CA."IdTipoOperacion"
		,MAX(GTA."INGFechaFactorMonedaAdicionalProximo")
		,GTA."INGValorFactorMonedaInstrumentoActual"
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND GTA."INGFechaPagoCorteCupon" != GTA."INGFechaCorteCupon" THEN 'S' 
			ELSE 'N' END AS "UsaProximoFactorValorMoneda"
		,'' AS "INGIdTipoOperacion"
		,CA."IdCartera"
		,'GARANTIA_HISTORICA' as "QueryOrigen"
FROM       "BSCL.Custodia.BD::HIS.Garantia" AS GTA
INNER JOIN CuponesGarantiaHistorica AS HIS ON (HIS."FolioCarteraOrigen" = GTA."FolioCarteraOrigen" AND HIS."Nemotecnico" = GTA."Nemotecnico" AND HIS."MaxFechaRespaldo" = GTA."FechaRespaldo" )
INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" AS CA ON (CA."Nemotecnico" = GTA."Nemotecnico" AND GTA."IdCarteraOrigen" = CA."IdCartera" AND CA."FechaRespaldo" = GTA."FechaRespaldo")
INNER JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte"						AS CT ON (CT."IdContraparte" = GTA."IdContraparteGarantia")
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"							AS PE ON (PE."IdPersona" = CT."IdPersona")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"			AS DI ON (DI."IdPersona" = PE."IdPersona")
WHERE 	CA."IdClasificacionTipoInstrumento" = 'RF'
AND		GTA."INGFechaPagoCorteCupon" BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta
AND     GTA."FechaLiquidacion" 		      <= :FechaCorteCuponDesde
AND     CA."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CA."IdInstrumento" ELSE :IdInstrumento END)
AND     CA."IdBoveda" 					   <> 'CIA'				
AND 	GTA."IdEstado" = 'VIG'             -- La garantia debe estar Vigente
AND 	CA."IdEstado" IN ('LIQ', 'RPA')  -- La cartera que genero la garantia debe haber quedado liquidada o retirada parcialmente
AND	  	"MaxFechaRespaldo"				< GTA."INGFechaPagoCorteCupon"

GROUP BY 
	CA."IdCartera",
	DI."DocumentoIdentidad",
	DI."NombreCompleto",
	PE."RazonSocial",
	CA."IdClasificacionTipoInstrumento",	
	CA."IdTipoInstrumento",		
	CA."IdInstrumento",
	GTA."IdPortafolio",
	CA."Nemotecnico",
	GTA."INGCantidad",
	GTA."INGIdMonedaPagoCupon",						
	GTA."INGNumeroCupon",
	GTA."INGFechaPagoCorteCupon",
	GTA."INGFechaCorteCupon",
	CA."IdTipoReajuste",
	GTA."INGNumeroCupon",
	GTA."INGMontoInteresCorteCupon",
	CA."IdBoveda",					
	CA."IdBolsa",
	CA."IdTipoReajuste",
	CA."IdEstado",
	GTA."INGMontoCapitalCorteCupon",
	GTA."FechaLiquidacion",
	GTA."INGMontoCapitalCorteCuponMonedaInstrumento",
	GTA."INGValorFactorMonedaInstrumentoProximo",
	GTA."INGFechaCorteCupon",
	CA."IdTipoOperacion",
    GTA."INGFechaFactorMonedaAdicionalProximo",
	GTA."INGValorFactorMonedaInstrumentoActual",
	GTA."INGMontoInteresCorteCuponMonedaInstrumento",
	GTA."INGMontoCorteCupon",
	GTA."INGMontoCorteCuponMonedaInstrumento"