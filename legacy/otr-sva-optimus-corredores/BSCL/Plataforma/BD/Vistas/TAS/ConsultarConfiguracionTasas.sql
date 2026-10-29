SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarConfiguracionTasas";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarConfiguracionTasas" AS
SELECT
	 CT."IdConfiguracionTasas"
	,CT."IdEmpresa"
	,CT."NombreEntidad"
	,CT."Valor"
	,CT."Descripcion"
FROM "BSCL.Plataforma.BD::TAS.ConfiguracionTasas" CT;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TAS.ConsultarConfiguracionTasas" TO BCS_DESARROLLADOR WITH GRANT OPTION;
