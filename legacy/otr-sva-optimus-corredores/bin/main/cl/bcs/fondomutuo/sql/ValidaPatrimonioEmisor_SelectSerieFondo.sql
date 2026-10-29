SELECT 
S."IdSerieFondo",
F."IdMoneda",
F."IdEmisor",
S."MontoPatrimonioEmisor"
FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS S
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS F ON (F."IdFondo" = S."IdFondo")
WHERE S."IdSerieFondo" IN (:IdSerieFondo);