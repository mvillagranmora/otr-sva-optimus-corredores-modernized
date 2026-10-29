SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarTemplateMesa";
 
CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarTemplateMesa" AS

SELECT
	TME."IdTemplateMesa",
	TEM."Id" AS "IdReporte",
	TEM."ReportName" AS "NombreReporte",
	TME."IdReporte" AS "IdTemplateMesaSubreporte",
	TME."TipoReporte",
	TME."ServicioReporte",
	TME."Descripcion",
	TME."IdEmpresa",
	TME."IdEstado"
	--,TEM.*
FROM "BSCL.Mesa.BD::MES.TemplateMesa" AS TME
INNER JOIN  "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTemplate" TEM
	ON (TME."IdEmpresa" = TEM."CompanyId" and TME."NombreReporte" = TEM."ReportName");


GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarTemplateMesa" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
