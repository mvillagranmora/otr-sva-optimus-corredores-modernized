
SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::CAR.ConsultarTipoCargaMasiva";
GO 
CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::CAR.ConsultarTipoCargaMasiva" AS

SELECT 
	TCM."IdTipoCargaMasiva",
	TCM."CodigoTipoCargaMasiva",
	TCM."Nombre",
	TCM."IdEmpresa",
	TCM."IdEstado"	
FROM "BSCL.CuentaInversion.BD::CAR.TipoCargaMasiva" AS TCM
WITH READ ONLY;
GO 
GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::CAR.ConsultarTipoCargaMasiva" TO  BCS_DESARROLLADOR WITH GRANT OPTION;