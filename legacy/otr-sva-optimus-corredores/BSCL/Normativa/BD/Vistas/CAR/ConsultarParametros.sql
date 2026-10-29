SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarParametros";

CREATE VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarParametros" AS
    SELECT * FROM "BSCL.Normativa.BD::CAR.Parametro";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::CAR.ConsultarParametros" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
