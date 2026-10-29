
WITH EventoCapitalContraparte AS 
(
	SELECT
		CASE IFNULL(BOV."IdPersona",0)
			WHEN 0 THEN EMI."IdPersona"
			ELSE        BOV."IdPersona"
		 END AS "IdPersonaPagoCobro"
		,ECC."IdPortafolio" AS "IdPortafolioPagoCobro"				
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
	
	FROM "BSCL.Custodia.BD::CUS.EventoCapital"	 			AS EC
	LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente"	AS ECC ON (EC."IdEventoCapital" = ECC."IdEventoCapital" AND EC."IdTipoEventoCapital" = ECC."IdTipoEventoCapital")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda"              AS BOV ON (BOV."IdBoveda" = ECC."IdBoveda")
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento"			AS INS ON (ECC."IdInstrumento" = INS."IdInstrumento")
	LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" 				AS EMI ON (EMI."IdEmisor" = INS."IdEmisor")
	
	WHERE EC."IdEventoCapital"      = :IdEventoCapital    	
	AND   EC."IdTipoEventoCapital" 	= :IdTipoEventoCapital
	AND   ECC."TipoSocioNegocio"    = 'CNT'
)

SELECT 
	 row_number() OVER (order by "IdPersonaPagoCobro","IdPortafolioPagoCobro", "IdMoneda", "FechaCierre", "IdInstrumento","IdBoveda") AS "IdInterno"	 -- para contraparte se genera Id ya que es una agrupacion
	,"IdPersonaPagoCobro"
	,"IdPortafolioPagoCobro"	
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
FROM EventoCapitalContraparte
GROUP BY 	"IdPersonaPagoCobro",			
			"IdPortafolioPagoCobro",
			"IdMoneda",
			"FechaCierre",
			"IdInstrumento",
			"IdTipoInstrumento",
			"IdClasificacionTipoInstrumento",
			"MontoInteres",
			"Retencion",
			"IdBoveda";
			
			