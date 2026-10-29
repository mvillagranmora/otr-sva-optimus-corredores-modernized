WITH CodigoPortafolioActivo AS (
	SELECT "DocumentoIdentidad", "IdPersona", "CodigoPortafolio", "IdPortafolio"
	    FROM (
	        SELECT "DocumentoIdentidad", "IdPersona", "CodigoPortafolio", "IdPortafolio",
	               ROW_NUMBER() OVER (PARTITION BY "DocumentoIdentidad", "IdPersona" ORDER BY "CodigoPortafolio" ASC) AS rn
	        FROM "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"
	    ) 
    WHERE rn = 1
),
 EventoCapitalContraparte AS (
	SELECT
		CCP."IdPersona" AS "IdPersonaSocioNegocio"
		,COALESCE(CCP."IdPortafolio",0) AS "IdPortafolioSocioNegocio"
		,CCP."CodigoPortafolio"
		,ECC."IdMoneda"
		,ECC."FechaCierre"
		,ECC."IdInstrumento" 				
		,ECC."IdTipoInstrumento"			
		,ECC."IdClasificacionTipoInstrumento"		
		,ECC."IdBoveda"		
		,ECC."MontoBeneficioArchivo"
		,ECC."MontoRetencionArchivo"
		,ECC."Total"
		,ECC."MontoInteres"
		,CAST(0 AS DECIMAL(26,8)) AS "Retencion"
		, ECC."IdEventoCapitalCliente" 
		, ECC."IdEventoCapital"
	
	FROM "BSCL.Custodia.BD::CUS.EventoCapital"	 			AS EC
	LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente"	AS ECC ON (EC."IdEventoCapital" = ECC."IdEventoCapital" AND EC."IdTipoEventoCapital" = ECC."IdTipoEventoCapital")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 		AS SOC  ON (ECC."IdSocioNegocio" = SOC."IdSocioDeNegocio")
	INNER JOIN CodigoPortafolioActivo AS CCP ON CCP."DocumentoIdentidad" = ECC."DocumentoIdentidad"
	WHERE EC."IdEventoCapital"      = :IdEventoCapital    	
	AND   EC."IdTipoEventoCapital" 	= :IdTipoEventoCapital
	AND   ECC."Total" < 0
)

SELECT 
	 row_number() OVER (order by "IdPersonaSocioNegocio", "IdPortafolioSocioNegocio", "IdMoneda", "FechaCierre", "IdInstrumento","IdBoveda") AS "IdInterno"
	,"IdPersonaSocioNegocio"
	,"IdPortafolioSocioNegocio"	
	,"IdMoneda"
	,"FechaCierre"
	,"IdInstrumento" 				
	,"IdTipoInstrumento"			
	,"IdClasificacionTipoInstrumento"
	,"IdBoveda"
	,CASE :LocalizacionSistema
		WHEN 'PE' THEN   -- PERU
				CASE "IdBoveda"
					WHEN 'CAV' THEN  -- CAVALI
						CAST(Sum("MontoBeneficioArchivo") - sum("MontoRetencionArchivo") AS DECIMAL(26,8))
					ELSE
						CAST(Sum("Total") AS DECIMAL(26,8))
				END
		ELSE            -- CHILE
				CAST(Sum("Total") AS DECIMAL(26,8))
				
	END AS "Monto"
	,"MontoInteres"
	,"Retencion"
	,"IdEventoCapitalCliente"
	,"IdEventoCapital"
FROM EventoCapitalContraparte
GROUP BY 	"IdPersonaSocioNegocio",			
			"IdPortafolioSocioNegocio",
			"IdMoneda",
			"FechaCierre",
			"IdInstrumento",
			"IdTipoInstrumento",
			"IdClasificacionTipoInstrumento",
			"MontoInteres",
			"Retencion",
			"IdBoveda",
			"IdEventoCapitalCliente",
			"IdEventoCapital";
