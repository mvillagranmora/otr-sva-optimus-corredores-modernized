SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarIndicadoresPortafolioFondoMutuo";
 
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarIndicadoresPortafolioFondoMutuo"
AS 
SELECT	"IdPortafolio",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_SOBCTA' THEN "ValorIndicador" END),'N') AS "OperaLineaSobregiro"
FROM (

	SELECT "IdPortafolio","CodigoTipoIndicador","IdIndicador","ValorIndicador"
	FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"
) 
GROUP BY "IdPortafolio";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarIndicadoresPortafolioFondoMutuo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
