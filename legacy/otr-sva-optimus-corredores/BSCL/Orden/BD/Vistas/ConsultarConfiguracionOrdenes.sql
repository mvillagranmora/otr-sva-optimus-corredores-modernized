SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarConfiguracionOrdenes"; 

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarConfiguracionOrdenes"  
AS  
SELECT  CORD."IdEmpresa",
		CORD."CodigoParametro",
		CORD."Descripcion",
		CORD."Valor"
FROM "BSCL.Orden.BD::ORD.ConfiguracionOrdenes" CORD;

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarConfiguracionOrdenes" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
