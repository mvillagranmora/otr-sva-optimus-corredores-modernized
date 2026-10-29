SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarTemplateTesoreria";
GO
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarTemplateTesoreria" AS

SELECT
	TT."CodigoTemplateTesoreria",
	TT."IdTemplateTesoreria",
	TEM."Id" AS "IdReporte",
	TT."NombreReporte",
	TT."IdServicio",	
	SER."RutaServicio",
	TT."Descripcion",
	TT."IdEmpresa",
	TT."IdEstado",	
	TEM."ReportName",
	TEM."ReportFile",
	TEM."ReportVersion",	
	TT."FormatoSalida",  -- txt/pdf
	TT."IdBanco"
	
FROM "BSCL.Tesoreria.BD::TES.TemplateTesoreria" AS TT
INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTemplate" AS TEM ON (UPPER(TEM."ReportName")=UPPER(TT."NombreReporte") AND TT."IdEmpresa"=TEM."CompanyId")
INNER JOIN "BSCL.Plataforma.BD::PLA.Servicio" AS SER ON (SER."IdServicio" = TT."IdServicio")

WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarTemplateTesoreria" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
