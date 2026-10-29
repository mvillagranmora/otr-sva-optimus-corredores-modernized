SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Contabilidad.BD.Vistas::CNT.ConsultarTemplateContabilidad" ;
GO
CREATE VIEW "BSCL.Contabilidad.BD.Vistas::CNT.ConsultarTemplateContabilidad"  AS

SELECT
	TC."IdTemplateContabilidad",
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

FROM "BSCL.Contabilidad.BD::CNT.TemplateContabilidad"  AS TC
INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTemplate" AS TEM ON (UPPER(TEM."ReportName")=UPPER(TC."NombreReporte") AND TC."IdEmpresa"=TEM."CompanyId")
INNER JOIN "BSCL.Plataforma.BD::PLA.Servicio" AS SER ON (SER."IdServicio" = TC."IdServicio")
WHERE TEM."ReportFile" IS NOT NULL

WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Contabilidad.BD.Vistas::CNT.ConsultarTemplateContabilidad"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO