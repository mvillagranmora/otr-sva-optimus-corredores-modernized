SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::VAL.ConsultarCupones";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::VAL.ConsultarCupones" AS
    SELECT * FROM "BSCL.Plataforma.BD::INS.Cupon";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::VAL.ConsultarCupones" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
