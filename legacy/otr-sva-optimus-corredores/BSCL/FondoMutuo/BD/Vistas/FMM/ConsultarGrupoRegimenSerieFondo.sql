SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarGrupoRegimenSerieFondo";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarGrupoRegimenSerieFondo"
AS
SELECT DISTINCT
 "RegimenSerieDeFondo"."IdSerieFondo"
, "RegimenFondoMutuo"."GrupoRegimen"
, "RegimenFondoMutuo"."PermiteCambioGrupo"
, "RegimenSerieDeFondo"."IdEmpresa"
FROM "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" AS "RegimenSerieDeFondo"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS "RegimenFondoMutuo" ON ("RegimenSerieDeFondo"."IdRegimenFondoMutuo" = "RegimenFondoMutuo"."IdRegimenFondoMutuo")
WHERE "RegimenSerieDeFondo"."IdEstado" = 'ACT'; 

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarGrupoRegimenSerieFondo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
