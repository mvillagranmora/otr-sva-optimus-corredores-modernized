SET SCHEMA "BSCL"; 
--"BSCL.Orden.BD.Vistas::UI.ConsultarTipoCanalDistribucionBolsa"
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarConfiguracionCanalDistribucion";
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarConfiguracionCanalDistribucion"
AS SELECT 
	tcdb."IdTipoCanalDistribucion",
	tcd."Nombre" as "NombreTipoCanalDistribucion",
	tcdb."IdTipoCanalIngreso",
	tcdb."IdTipoInstrumento",
	tcdb."IdBolsa",
	tcdb."IdEmpresa"
	


FROM  "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion" tcdb
INNER JOIN "BSCL.Orden.BD::ORD.TipoCanalDistribucion" tcd on tcd."IdTipoCanalDistribucion" = tcdb."IdTipoCanalDistribucion";

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarConfiguracionCanalDistribucion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
