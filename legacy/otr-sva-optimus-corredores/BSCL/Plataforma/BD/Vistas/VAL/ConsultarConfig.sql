SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::VAL.ConsultarConfig";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::VAL.ConsultarConfig" AS
    SELECT * FROM "BSCL.Plataforma.BD::VAL.Config";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::VAL.ConsultarConfig" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
