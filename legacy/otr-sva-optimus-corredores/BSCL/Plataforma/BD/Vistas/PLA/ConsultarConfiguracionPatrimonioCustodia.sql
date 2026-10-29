SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionPatrimonioCustodia"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionPatrimonioCustodia" AS
SELECT
	 "CodigoItem"
	,"DescripcionItem"
	,"FuncionOperador"
	,"IdEstado"
FROM "BSCL.Plataforma.BD::PLA.ConfiguracionPatrimonioCustodia";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionPatrimonioCustodia" TO BCS_DESARROLLADOR WITH GRANT OPTION;
