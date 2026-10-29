SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarPlantillas";

CREATE VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarPlantillas" AS
    SELECT * FROM "BSCL.Normativa.BD::CAR.Plantilla";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::CAR.ConsultarPlantillas" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
