SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegimenSerieFondoAnalitica";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegimenSerieFondoAnalitica" AS
SELECT
    RegimenSerieDeFondo."IdRegimenFondoMutuo"
   ,RegimenSerieDeFondo."IdSerieFondo"
   ,Fondo."IdFondo"
   ,RegimenFondoMutuo."Descripcion" AS "DescripcionRegimen"
   ,RegimenSerieDeFondo."IdEstado" AS "IdEstadoRegimenSerieDeFondo"
   ,RegimenFondoMutuo."IdEstado" AS "IdEstadoRegimenFondoMutuo"
   ,CASE RegimenFondoMutuo."GrupoRegimen"
   		WHEN 'FMU' THEN 'NOAPV' 
   		WHEN 'APV-A' THEN 'APV' 
   		WHEN 'APV-B' THEN 'APV' 
   		WHEN 'DC' THEN 'APV' 
        ELSE RegimenFondoMutuo."GrupoRegimen"
    END AS "GrupoRegimen"
   ,RegimenFondoMutuo."PermiteOtrosGrupos"
   ,RegimenFondoMutuo."PermiteRescate"
   ,RegimenFondoMutuo."PermiteAporte"
   ,RegimenFondoMutuo."PermiteCambioGrupo"
FROM "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" AS RegimenSerieDeFondo
INNER JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS RegimenFondoMutuo 
    ON (RegimenSerieDeFondo."IdRegimenFondoMutuo" = RegimenFondoMutuo."IdRegimenFondoMutuo")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieFondo 
ON (SerieFondo."IdSerieFondo" = RegimenSerieDeFondo."IdSerieFondo" )
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo 
    ON (Fondo."IdFondo" = SerieFondo."IdFondo");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegimenSerieFondoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
