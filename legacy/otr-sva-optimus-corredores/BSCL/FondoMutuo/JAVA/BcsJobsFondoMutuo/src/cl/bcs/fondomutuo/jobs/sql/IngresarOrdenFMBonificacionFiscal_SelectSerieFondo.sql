SELECT 
 "SerieFondo"."IdSerieFondo",
 "SerieFondo"."DecimalesCuota"
FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo"
WHERE "SerieFondo"."IdEmpresa" = :IdEmpresa AND "SerieFondo"."IdSerieFondo" IN (:IdSerieFondo);