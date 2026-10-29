SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTemplate"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTemplate"  
AS
SELECT
	"CompanyId",
	"Id",
	"ReportName",
	"ReportFile",
	"ReportVersion"
FROM "BSCL.Report::Template"
WHERE IFNULL("MasterReportId",'MASTER') = 'MASTER';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTemplate" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
