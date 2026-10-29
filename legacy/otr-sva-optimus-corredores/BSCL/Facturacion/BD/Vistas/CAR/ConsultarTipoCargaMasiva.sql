SET SCHEMA "BSCL";
GO 

DROP VIEW "BSCL.Facturacion.BD.Vistas::CAR.ConsultarTipoCargaMasiva";
GO 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::CAR.ConsultarTipoCargaMasiva" AS

SELECT 
	TCM."IdTipoCargaMasiva",
	TCM."CodigoArchivo",
	TCM."Nombre",
	TCM."IdEmpresa",
	TCM."IdEstado"
	
FROM "BSCL.Facturacion.BD::CAR.TipoCargaMasiva" AS TCM
WITH READ ONLY;
GO 

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::CAR.ConsultarTipoCargaMasiva" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO 