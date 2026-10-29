SELECT "SerieFondo"."IdSerieFondo",
       "SerieFondo"."IdInstrumento",
       "Fondo"."IdTipoInstrumento"
FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo"
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieFondo"."IdFondo" AND "Fondo"."IdEmpresa" = :IdEmpresa)
WHERE "SerieFondo"."IdEmpresa" = :IdEmpresa AND "SerieFondo"."IdSerieFondo" IN (:IdSerieFondo);
