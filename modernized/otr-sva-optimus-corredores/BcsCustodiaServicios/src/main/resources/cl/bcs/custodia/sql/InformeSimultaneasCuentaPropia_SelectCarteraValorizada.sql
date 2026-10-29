
WITH 
	DatosValorizados AS (
	SELECT 
		C."IdInforme", 
		row_number() OVER (PARTITION BY C."IdEmpresa" ORDER BY C."IdInforme" DESC) AS "Rank" 
	FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" AS C
	WHERE C."FechaValorizacionActual" = :FechaValorizacion
	AND C."IdPortafolio" = :IdPortafolio
	GROUP BY C."IdInforme", C."IdEmpresa"
	ORDER BY "Rank" ASC
), IdsValorizaciones AS (
	SELECT 
		DV."IdInforme"
	FROM DatosValorizados AS DV
	WHERE DV."Rank" = 1
)
SELECT 
	* 
FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" AS C
INNER JOIN IdsValorizaciones IV ON C."IdInforme" = IV."IdInforme"