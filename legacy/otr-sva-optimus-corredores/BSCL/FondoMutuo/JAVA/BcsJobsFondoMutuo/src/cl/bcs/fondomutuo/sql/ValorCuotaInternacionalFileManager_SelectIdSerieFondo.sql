SELECT "SerieDeFondo"."ISIN","SerieDeFondo"."IdSerieFondo","Fondo"."IdMoneda"
FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo"
ON "SerieDeFondo"."IdFondo" = "Fondo"."IdFondo" 
WHERE "ISIN" = :isin