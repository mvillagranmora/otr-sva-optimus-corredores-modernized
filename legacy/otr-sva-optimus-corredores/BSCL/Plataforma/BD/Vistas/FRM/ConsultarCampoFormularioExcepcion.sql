SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::FRM.ConsultarCampoFormularioExcepcion";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::FRM.ConsultarCampoFormularioExcepcion"
AS

SELECT
	CFE."CodigoFormulario"						AS "CodigoFormulario"
	 , CFE."ValorCampoExcepcion"				AS "ValorCampoExcepcion"
	 , CFE."IdTipoCanal"						AS "IdTipoCanal"
	 , Campo."CodigoCampo"						AS "CodigoCampo"
	 , Campo."CodigoCampoBackend"				AS "CodigoCampoBackend"
	 , CampoExcepcion."CodigoCampo"				AS "CodigoCampoExcepcion"
	 , CampoExcepcion."CodigoCampoBackend"		AS "CodigoCampoBackendExcepcion"
FROM "BSCL.Plataforma.BD::FRM.CampoFormularioExcepcion" CFE
	 LEFT JOIN "BSCL.Plataforma.BD::FRM.Campo" Campo ON (Campo."CodigoCampo" = CFE."CodigoCampo" AND Campo."CodigoCampo" IS NOT NULL)
	 LEFT JOIN "BSCL.Plataforma.BD::FRM.Campo" CampoExcepcion ON (CampoExcepcion."CodigoCampo" = CFE."CodigoCampoExcepcion" AND CampoExcepcion."CodigoCampo" IS NOT NULL);

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::FRM.ConsultarCampoFormularioExcepcion" TO BCS_DESARROLLADOR WITH GRANT OPTION;