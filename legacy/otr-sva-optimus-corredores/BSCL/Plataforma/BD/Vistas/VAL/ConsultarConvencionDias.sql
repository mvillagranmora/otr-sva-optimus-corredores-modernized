SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::VAL.ConsultarConvencionDias";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::VAL.ConsultarConvencionDias" AS
    SELECT * FROM "BSCL.Plataforma.BD::VAL.ConvencionDias";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::VAL.ConsultarConvencionDias" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
