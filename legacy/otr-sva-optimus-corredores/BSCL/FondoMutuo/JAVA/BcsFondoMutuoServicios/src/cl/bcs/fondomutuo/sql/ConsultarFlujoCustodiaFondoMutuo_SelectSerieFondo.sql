SELECT SERIE."IdInstrumento",SERIE."IdSerieFondo",FONDO."IdMoneda"
FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SERIE
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" FONDO ON SERIE."IdFondo" = FONDO."IdFondo"
WHERE SERIE."IdInstrumento" IN (:IdInstrumento)