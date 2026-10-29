SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegimenSerieDeFondo";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegimenSerieDeFondo"
AS
SELECT
 "RegimenSerieDeFondo"."IdRegimenFondoMutuo"
, "RegimenSerieDeFondo"."IdSerieFondo"
, "RegimenFondoMutuo"."Descripcion"
, "RegimenSerieDeFondo"."IdEstado" AS "IdEstadoRegimenSerieDeFondo"
, "RegimenFondoMutuo"."IdEstado" AS "IdEstadoRegimenFondoMutuo"
, "RegimenFondoMutuo"."GrupoRegimen"
, "RegimenFondoMutuo"."PropositoSerieRelacionado"
, "RegimenFondoMutuo"."PermiteOtrosGrupos"
, "RegimenFondoMutuo"."ValidaPortafolioCliente"
, "RegimenFondoMutuo"."ValidaPortafolioAGF"
, "RegimenFondoMutuo"."PermiteRescate"
, "RegimenFondoMutuo"."TipoCarteraNormativa"
, "RegimenFondoMutuo"."PermiteCambioGrupo"
, "RegimenFondoMutuo"."RescateConMandato"
, "RegimenFondoMutuo"."AceptaMandatoEnReinversion"
, "RegimenSerieDeFondo"."IdEmpresa"
, SF."NombreSerieFondo"
, F."NombreFondo"
FROM "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" AS "RegimenSerieDeFondo"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS "RegimenFondoMutuo" ON ("RegimenSerieDeFondo"."IdRegimenFondoMutuo" = "RegimenFondoMutuo"."IdRegimenFondoMutuo")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SF ON (SF."IdSerieFondo" = "RegimenSerieDeFondo"."IdSerieFondo" )
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS F ON (F."IdFondo" = SF."IdFondo");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegimenSerieDeFondo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
