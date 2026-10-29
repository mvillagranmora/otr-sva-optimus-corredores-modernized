SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarQuincenalDePactos";
GO
CREATE VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarQuincenalDePactos" AS

SELECT *
FROM "BSCL.Mesa.BD::MES.QuincenalPactos";
GO
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::MRF.ConsultarQuincenalDePactos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
