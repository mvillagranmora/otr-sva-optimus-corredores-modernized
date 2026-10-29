SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarTemplateCustodia" ;
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarTemplateCustodia"  AS

SELECT
	TC."IdTemplateCustodia",
	TEM."Id" AS "IdReporte",
	TC."NombreReporte",
	TC."NombreSubReporte",
	TC."IdServicio",	
	SER."RutaServicio",
	TC."Descripcion",
	TC."IdEmpresa",
	TC."IdEstado",	
	TEM."ReportName",
	TEM."ReportFile",
	TEM."ReportVersion",	
	TC."FormatoSalida"  -- txt/pdf

FROM "BSCL.Custodia.BD::CUS.TemplateCustodia"  AS TC
INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTemplate" AS TEM ON (UPPER(TEM."ReportName")=UPPER(TC."NombreReporte") AND TC."IdEmpresa"=TEM."CompanyId")
INNER JOIN "BSCL.Plataforma.BD::PLA.Servicio" AS SER ON (SER."IdServicio" = TC."IdServicio")
WHERE TEM."ReportFile" IS NOT NULL

WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarTemplateCustodia"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO