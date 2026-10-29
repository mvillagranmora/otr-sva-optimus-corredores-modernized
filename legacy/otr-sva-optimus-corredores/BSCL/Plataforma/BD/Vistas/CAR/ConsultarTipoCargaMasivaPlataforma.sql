SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::CAR.ConsultarTipoCargaMasivaPlataforma";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::CAR.ConsultarTipoCargaMasivaPlataforma" AS

SELECT 
	TCM."IdTipoCargaMasiva",
	TCM."Codigo",
	TCM."Nombre",
	TCM."IdEmpresa",
	TCM."IdEstado"
FROM "BSCL.Plataforma.BD::PLA.TipoCargaMasiva" AS TCM

WITH READ ONLY;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::CAR.ConsultarTipoCargaMasivaPlataforma" TO  BCS_DESARROLLADOR WITH GRANT OPTION;


