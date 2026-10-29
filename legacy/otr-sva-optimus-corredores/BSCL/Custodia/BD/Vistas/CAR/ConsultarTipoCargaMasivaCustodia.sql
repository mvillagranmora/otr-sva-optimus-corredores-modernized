
SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::CAR.ConsultarTipoCargaMasivaCustodia";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CAR.ConsultarTipoCargaMasivaCustodia" AS

SELECT 
	TCM."IdTipoCargaMasiva",
	TCM."Codigo",
	TCM."Nombre",
	TCM."IdEmpresa",
	TCM."IdEstado"
FROM "BSCL.Custodia.BD::CUS.TipoCargaMasiva" AS TCM

WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CAR.ConsultarTipoCargaMasivaCustodia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;


