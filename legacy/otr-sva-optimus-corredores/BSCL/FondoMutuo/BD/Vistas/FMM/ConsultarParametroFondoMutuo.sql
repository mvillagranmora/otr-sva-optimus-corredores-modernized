SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarParametroFondoMutuo";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarParametroFondoMutuo"
AS 
SELECT
 "Parametro"."IdEmpresa"
, "Parametro"."IdParametro"
, "Parametro"."NombreTipoParametro"
, "Parametro"."ValorParametro"
, "Parametro"."NombreParametro"
, "Parametro"."IdEstado"
, "Estado"."Nombre" AS "NombreEstado"
FROM "BSCL.FondoMutuo.BD::FMM.Parametro" AS "Parametro"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS "Estado" ON ("Parametro"."IdEstado" = "Estado"."IdEstado");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarParametroFondoMutuo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;