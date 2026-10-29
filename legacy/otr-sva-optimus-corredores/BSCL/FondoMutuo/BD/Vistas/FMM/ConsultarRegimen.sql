SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegimen";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegimen"
AS
SELECT 
 "Regimen"."IdRegimenFondoMutuo"
, "Regimen"."Descripcion"
, "Regimen"."GrupoRegimen"
, "Regimen"."PermiteOtrosGrupos"
, "Regimen"."ValidaPortafolioCliente"
, "Regimen"."ValidaPortafolioAGF"
, "Regimen"."PropositoSerieRelacionado"
, "Proposito"."Descripcion" AS "CodigoPortafolio"
, "Regimen"."PermiteRescate"
, "Regimen"."PermiteAporte"
, "Regimen"."PermiteTraspaso"
, "Regimen"."ValidaMontoMinimoInstrumento"
, "Regimen"."TipoCarteraNormativa"
, "Regimen"."PermiteCambioGrupo"
, "Regimen"."RescateConMandato"
, "Regimen"."AceptaMandatoEnReinversion"
, "Regimen"."EsPorDefecto"
, "Regimen"."IdEstado"
, "Regimen"."CertificaSinAsociado"
, "Regimen"."HomologacionNCG365" 
FROM "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS "Regimen"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPropositoPortafolio" AS "Proposito" ON ("Proposito"."IdPropositoPortafolio" = "Regimen"."PropositoSerieRelacionado");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegimen" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

