SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Mesa.BD.Vistas::CAR.ConsultarTipoCargaMasivaRF";
GO 
CREATE VIEW "BSCL.Mesa.BD.Vistas::CAR.ConsultarTipoCargaMasivaRF" AS

SELECT 
	"IdTipoCargaMasiva",
	"CodigoTipoCargaMasiva",
	"Nombre",
	"IdEmpresa",
	"IdEstado"
	
FROM "BSCL.Mesa.BD::CAR.TipoCargaMasivaRF"
WITH READ ONLY;
GO 
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::CAR.ConsultarTipoCargaMasivaRF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;