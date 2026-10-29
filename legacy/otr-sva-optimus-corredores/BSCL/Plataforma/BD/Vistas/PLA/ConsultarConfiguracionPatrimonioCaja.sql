SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionPatrimonioCaja"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionPatrimonioCaja" AS
SELECT
	 "CodigoItem"
	,"DescripcionItem"
	,"FuncionOperador"
	,"IdEstado"
FROM "BSCL.Plataforma.BD::PLA.ConfiguracionPatrimonioCaja";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionPatrimonioCaja" TO BCS_DESARROLLADOR WITH GRANT OPTION;
