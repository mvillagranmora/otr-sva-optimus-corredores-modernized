SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarEstadoDeposito";
 
CREATE VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarEstadoDeposito" AS

SELECT
	ED."IdEstadoDeposito",
	ED."IdEstado",
	ED."NombreEntidad",
	ED."Descripcion"
FROM "BSCL.Mesa.BD::MES.EstadoDeposito" AS ED;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::MRF.ConsultarEstadoDeposito" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
