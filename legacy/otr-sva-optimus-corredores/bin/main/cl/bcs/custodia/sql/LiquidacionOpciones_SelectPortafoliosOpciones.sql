	SELECT 
		"PortafolioDerivados"	AS "IdPortafolio",
		"NumeroReferencia"		AS "NumeroReferencia"
    FROM 		"BSCL.Custodia.BD::CUS.SorteoContratos"
    WHERE "Fecha" = :FechaSistema
