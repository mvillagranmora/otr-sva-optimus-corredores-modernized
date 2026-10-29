SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoCombobox";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoCombobox"  
AS
SELECT
"Fondo"."IdFondo" AS "IdFondo"
, "Fondo"."Nemotecnico" AS "Nemotecnico"
, "Fondo"."FondoNormativo"
FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoCombobox" TO  BCS_DESARROLLADOR WITH GRANT OPTION;