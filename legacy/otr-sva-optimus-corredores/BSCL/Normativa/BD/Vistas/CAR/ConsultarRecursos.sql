SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarRecursos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarRecursos" AS
    SELECT * FROM "BSCL.Normativa.BD::CAR.Recurso"
    WHERE "Grupo" <> 'SISTEMA';

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::CAR.ConsultarRecursos"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
