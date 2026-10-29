SET SCHEMA "BSCL";
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarTemplateCuentaInversion";

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarTemplateCuentaInversion" AS
SELECT
	TCTA."IdTemplateCuentaInversion",
	TE."Id" as "IdReporte",
	TCTA."NombreReporte",
	TCTA."IdServicio",
	TCTA."Descripcion",
	TCTA."IdEmpresa",
	TCTA."IdEstado",	
	TE."ReportName",
	TE."MasterReportId",
	TE."ReportFile",
	TE."ReportVersion",
	TCTA."FormatoSalida"  -- txt/pdf
FROM "BSCL.CuentaInversion.BD::CTA.TemplateCuentaInversion" AS TCTA
INNER JOIN "BSCL.Report::Template" AS TE ON TE."ReportName" = Upper(TCTA."NombreReporte") AND TE."ReportVersion" = TCTA."Version"
WITH READ ONLY;

GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarTemplateCuentaInversion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;