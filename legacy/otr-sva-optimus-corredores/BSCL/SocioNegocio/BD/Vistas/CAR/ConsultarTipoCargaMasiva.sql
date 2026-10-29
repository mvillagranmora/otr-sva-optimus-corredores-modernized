SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CAR.ConsultarTipoCargaMasiva";
GO 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CAR.ConsultarTipoCargaMasiva" AS
SELECT 
	TCM."IdTipoCargaMasiva",
	TCM."CodigoArchivo",
	TCM."Nombre",
	TCM."IdEmpresa",
	TCM."IdEstado"
	
FROM "BSCL.SocioNegocio.BD::CAR.TipoCargaMasiva" AS TCM;

GO 
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CAR.ConsultarTipoCargaMasiva" TO  BCS_DESARROLLADOR WITH GRANT OPTION;