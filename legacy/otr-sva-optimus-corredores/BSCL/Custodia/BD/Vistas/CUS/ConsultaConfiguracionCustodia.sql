SET SCHEMA "BSCL";
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultaConfiguracionCustodia";
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultaConfiguracionCustodia" 
AS

	SELECT
		"IdEmpresa",
		"CodigoParametro",
		"Descripcion",
		"Valor"
	FROM "BSCL.Custodia.BD::CUS.Configuracion";

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultaConfiguracionCustodia" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
