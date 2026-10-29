
SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Tesoreria.BD.Vistas::CAR.ConsultarTipoCargaMasiva";
GO 
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::CAR.ConsultarTipoCargaMasiva" AS

SELECT 
	TCM."IdTipoCargaMasiva",
	TCM."CodigoTipoCargaMasiva",
	TCM."Nombre",
	TCM."IdEmpresa",
	TCM."IdEstado"
	
FROM "BSCL.Tesoreria.BD::CAR.TipoCargaMasiva" AS TCM
WITH READ ONLY;
GO 
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::CAR.ConsultarTipoCargaMasiva" TO  BCS_DESARROLLADOR WITH GRANT OPTION;


