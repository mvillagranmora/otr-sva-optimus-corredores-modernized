SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarFactorIpc";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarFactorIpc" (
	"Agno"
	,"AgnoMes"
	,"AgnoTributario"
	,"DescMes"
	,"Fecha"
	,"Factor"
	,"ValorIpc"
) AS
SELECT
	"Agno"
	,"AgnoMes"
	,"AgnoTributario"
	,"DescMes"
	,"Fecha"
	,"Factor"
	,"ValorIpc"
FROM "BSCL.Normativa.BD::TRI.FactorIpc"
ORDER BY "AgnoMes" ASC
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.ConsultarFactorIpc" TO bcs_desarrollador WITH GRANT OPTION;
