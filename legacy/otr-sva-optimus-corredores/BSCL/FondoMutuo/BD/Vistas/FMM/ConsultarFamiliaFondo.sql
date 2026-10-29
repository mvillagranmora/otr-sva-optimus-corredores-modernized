SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFamiliaFondo";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFamiliaFondo"
AS
SELECT
 "FamiliaFondo"."IdFamiliaFondo"
, "FamiliaFondo"."NombreFamilia"
, "ESTADO"."Nombre" as "Estado"
, "ESTADO"."IdEstado" as "IdEstado"
, "FamiliaFondo"."IdEmpresa"
FROM "BSCL.FondoMutuo.BD::FMM.FamiliaFondo" AS "FamiliaFondo"
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" "ESTFM" ON "FamiliaFondo"."IdEstado" = "ESTFM"."IdEstado"
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS "ESTADO" ON "ESTFM"."IdEstado" = "ESTADO"."IdEstado"
WHERE "ESTFM"."NombreEntidad" = UPPER('FamiliaFondo');

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFamiliaFondo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
