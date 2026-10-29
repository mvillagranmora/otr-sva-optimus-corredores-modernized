SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarInformesValorizados";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarInformesValorizados"
AS 
WITH "IdsInformes" AS (
	SELECT 
		"IdValorizacion", 
		"IdInforme",
		SUBSTRING("IdInforme", 0, LENGTH("IdInforme") - 19) AS "Key",
		SUBSTRING("IdInforme", LENGTH("IdInforme") - 18, LENGTH("IdInforme"))  AS "FechaHora" ,
		"IdClienteSolicitante",
		COALESCE("IdSocioDeNegocio", 0) AS "IdSocioDeNegocio"
	FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" INF	
	LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON SOC."IdCliente" = INF."IdClienteSolicitante"
),
"InformesOrdenados" AS (
	SELECT 
		"IdInforme",
		"Key",
		"FechaHora",
		ROW_NUMBER () OVER (PARTITION BY "Key", "IdClienteSolicitante", "IdSocioDeNegocio" ORDER BY "IdValorizacion" DESC) AS "Rank",
		"IdClienteSolicitante" AS "IdCliente",
		"IdSocioDeNegocio"
	FROM "IdsInformes"
)
SELECT 
	"IdInforme",
	"Key",
	"FechaHora",
	"Rank",
	"IdCliente",
	"IdSocioDeNegocio"
FROM "InformesOrdenados"
WHERE "Rank" = 1
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarInformesValorizados" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
GO
