SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionOpciones"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionOpciones" AS
SELECT
	 "IdEmpresa"
	,"IdParametro"
	,"NombreTipoParametro"
	,"ValorParametro"
	,"NombreParametro"
	,"IdEstado"
FROM "BSCL.Plataforma.BD::DER.Configuracion";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionOpciones" TO BCS_DESARROLLADOR WITH GRANT OPTION;
