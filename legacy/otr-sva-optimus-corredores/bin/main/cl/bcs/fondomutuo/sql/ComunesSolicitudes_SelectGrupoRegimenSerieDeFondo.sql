SELECT
 "GrupoRegimen"."IdSerieFondo" || '_' || + "GrupoRegimen"."GrupoRegimen" AS "IdGrupoRegimenSerieFondo"
,"GrupoRegimen"."IdSerieFondo"
,"GrupoRegimen"."GrupoRegimen"
,"GrupoRegimen"."PermiteCambioGrupo"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarGrupoRegimenSerieFondo" AS "GrupoRegimen" 
WHERE "GrupoRegimen"."IdSerieFondo" IN (:IdSerieFondo) AND "GrupoRegimen"."IdEmpresa" = :IdEmpresa;