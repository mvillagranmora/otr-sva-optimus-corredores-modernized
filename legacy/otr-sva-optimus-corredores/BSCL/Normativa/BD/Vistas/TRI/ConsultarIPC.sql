SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarIPC";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarIPC" AS
SELECT PF."Valor", PF."AgnoMes", PF."IdParametroFinanciero" 
FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" AS PF
WHERE PF."IdParametroFinanciero" = 'IPC'
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.ConsultarIPC" TO bcs_desarrollador WITH GRANT OPTION;