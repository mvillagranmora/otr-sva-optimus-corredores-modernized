SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::CAR.ConsultarConfiguracionCargaMasivaCustodia";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CAR.ConsultarConfiguracionCargaMasivaCustodia" AS

SELECT
	 CCM."IdConfiguracionCargaMasivaCustodia"
	,CCM."IdTipoCargaMasiva"
	,TCM."Nombre" 								AS "NombreTipoCargaMasiva"
	,CCM."IdConcepto"
	,CON."Nombre" 								AS "NombreConcepto"	
	,CCM."IdEmpresa"
	,CCM."IdBolsa"
	,CCM."IdBoveda"
	,CCM."IdEstado"
	,EST."Nombre" AS "NombreEstado"
			
FROM "BSCL.Custodia.BD::CUS.ConfiguracionCargaMasivaCustodia" AS CCM
LEFT OUTER JOIN "BSCL.Custodia.BD::CUS.TipoCargaMasiva" AS TCM ON (TCM."IdTipoCargaMasiva" = CCM."IdTipoCargaMasiva")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto" AS CON ON (CON."IdConcepto" = CCM."IdConcepto")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS EST ON (EST."IdEstado" = CCM."IdEstado")

WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CAR.ConsultarConfiguracionCargaMasivaCustodia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
