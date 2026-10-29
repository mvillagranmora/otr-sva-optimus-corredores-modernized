SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoTributario";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoTributario"  
AS
SELECT
"Fondo"."IdFondo" AS "IdFondo"
, "Fondo"."Nemotecnico" AS "Nemotecnico"
, "Fondo"."FondoNormativo"
, "Fondo"."CodigoSVS"
, "Fondo"."NombreFondo"
FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoTributario" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
