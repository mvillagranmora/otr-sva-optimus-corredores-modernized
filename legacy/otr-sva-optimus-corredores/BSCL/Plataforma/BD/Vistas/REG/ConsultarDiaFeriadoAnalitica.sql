SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarDiaFeriadoAnalitica";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarDiaFeriadoAnalitica" AS
SELECT
"IdFeriado"
, "Tipo"
, LEFT("Fecha",10) AS "Fecha"
, TO_BIGINT("Dia") AS "Dia"
, "DescripcionDia"
, TO_BIGINT("AgnoCarga") as "AgnoCarga"
, "IdPais"
, "IdEstado"
FROM "BSCL.Plataforma.BD::REG.DiaFeriado";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REG.ConsultarDiaFeriadoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
