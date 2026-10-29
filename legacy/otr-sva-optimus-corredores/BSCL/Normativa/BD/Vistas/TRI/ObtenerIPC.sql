SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.ObtenerIPC";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.ObtenerIPC" (
	"Esquema"
	,"Version"
	,"Valor"
	,"Agno"
	,"Mes"
) AS
SELECT ESQ."Esquema",
       ESQ."Version",
       PM."Valor", 
	   YEAR(PM."Fecha") AS "Agno", 
	   MONTH(PM."Fecha") AS "Mes"
FROM "BSCL.Normativa.BD::DOC.Esquema" ESQ
LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PM on CAST(YEAR(PM."Fecha") AS NVARCHAR) = ESQ."Version" AND ESQ."Periodicidad" = 'A'
WHERE "IdParametroFinanciero" = 'IPC'
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.ObtenerIPC" TO bcs_desarrollador WITH GRANT OPTION;
