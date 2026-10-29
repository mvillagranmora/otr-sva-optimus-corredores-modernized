
WITH
	IndicadoresPortafolio AS (
		SELECT
			 OPE."NumeroOperacion"
			,OPE."IdPortafolio"
			,OPE."IdTipoOperacion"
			,OPE."IdMonedaPrincipal"     
			,OPE."IdMonedaSecundaria"     
			,OPE."MontoMonedaPrincipal"   
			,OPE."MontoMonedaSecundaria"
			-- Si operacion Spot proviene de FMM no se debe validar Flujo de Caja
			----,(CASE WHEN VALIND."OperaCuentaInversion" = 'S' AND VALIND."OperaLineaSobregiro" = 'N' THEN 'S' ELSE 'N' END) AS "ValidaSaldoCaja"
			,(CASE WHEN OPE."IdTipoCanalIngreso" <> 'FMM' AND VALIND."OperaCuentaInversion" = 'S' AND VALIND."OperaLineaSobregiro" = 'N' THEN 'S' ELSE 'N' END) AS "ValidaSaldoCaja"
			
			,OPE."IdTipoCanalIngreso"  
		FROM "BSCL.Mesa.BD::SPT.CrearOperacionSpot_Proceso" AS OPE
		
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR
			ON (OPE."IdPortafolio"= POR."IdPortafolio")
		LEFT JOIN (
				SELECT	
					"IdPortafolio",
					COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_OCCINV' THEN "ValorIndicador" END),'N') AS "OperaCuentaInversion",
					COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_SOBCTA' THEN "ValorIndicador" END),'N') AS "OperaLineaSobregiro"
				FROM (
					SELECT
						OPE."IdPortafolio",
						INDPOR."CodigoTipoIndicador",
						INDPOR."IdIndicador",
						INDPOR."ValorIndicador"
					FROM "BSCL.Mesa.BD::SPT.CrearOperacionSpot_Proceso" AS OPE	
					INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS INDPOR
						ON (INDPOR."IdPortafolio" = OPE."IdPortafolio" AND
							INDPOR."CodigoTipoIndicador" IN ('GEN_OCCINV','GEN_SOBCTA'))
					WHERE OPE."NumeroOperacion" IN (:NumeroOperacion)) IND 
				GROUP BY IND."IdPortafolio") VALIND
			ON (VALIND."IdPortafolio" = OPE."IdPortafolio")
		WHERE OPE."NumeroOperacion" IN (:NumeroOperacion)
	)
SELECT
	"NumeroOperacion",
	"IdPortafolio",
	"IdMoneda",
	SUM("Monto") AS "Monto"
FROM (
	SELECT
		 "NumeroOperacion"
		,"IdPortafolio"
		,CASE WHEN "IdTipoOperacion" = 'VENSPT' THEN "IdMonedaSecundaria"
			  WHEN "IdTipoOperacion" = 'COMSPT' THEN "IdMonedaPrincipal"
			  END AS "IdMoneda"
	 	,CASE WHEN "IdTipoOperacion" = 'VENSPT' THEN "MontoMonedaSecundaria"
	 		  WHEN "IdTipoOperacion" = 'COMSPT' THEN "MontoMonedaPrincipal"
	 		  END AS "Monto"
 	FROM IndicadoresPortafolio
 	WHERE "ValidaSaldoCaja" = 'S')
GROUP BY "NumeroOperacion","IdPortafolio","IdMoneda";
