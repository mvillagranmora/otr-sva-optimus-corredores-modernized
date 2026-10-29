SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarConfig";

CREATE VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarConfig" AS
    SELECT * FROM "BSCL.Normativa.BD::CAR.Config";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::CAR.ConsultarConfig" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
