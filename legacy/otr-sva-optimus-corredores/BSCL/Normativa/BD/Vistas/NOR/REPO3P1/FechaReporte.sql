SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.FechaReporte";
		  
CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.FechaReporte"(
	"FechaReporte"
) AS
	SELECT 
		CASE LTRIM(RTRIM(COALESCE(NOR."Valor",PLA."Valor"))) WHEN 	'SISTEMA'  THEN LTRIM(RTRIM(PLA."Valor"))
		ELSE LTRIM(RTRIM(COALESCE(NOR."Valor",PLA."Valor")))
		END AS "FechaReporte"
	FROM "BSCL.Plataforma.BD::PLA.Configuracion" PLA
	LEFT OUTER JOIN "BSCL.Normativa.BD::NOR.Configuracion" NOR ON (NOR."CodigoParametro"='BCC_REPO3P1_FECHAREPORTE'  AND NOR."IdNormativa"='REPO3P1')
	WHERE PLA."CodigoParametro"='PLA.FechaSistema' 

WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.FechaReporte" TO bcs_desarrollador WITH GRANT OPTION;
