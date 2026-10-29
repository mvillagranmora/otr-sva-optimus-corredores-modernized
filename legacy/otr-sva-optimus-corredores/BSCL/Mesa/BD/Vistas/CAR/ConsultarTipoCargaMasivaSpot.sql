
SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Mesa.BD.Vistas::CAR.ConsultarTipoCargaMasivaSpot";
GO 
CREATE VIEW "BSCL.Mesa.BD.Vistas::CAR.ConsultarTipoCargaMasivaSpot" AS

SELECT 
	"IdTipoCargaMasiva",
	"CodigoTipoCargaMasiva",
	"Nombre",
	"IdEmpresa",
	"IdEstado"
FROM "BSCL.Mesa.BD::CAR.TipoCargaMasivaSpot"
WITH READ ONLY;
GO 
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::CAR.ConsultarTipoCargaMasivaSpot" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
