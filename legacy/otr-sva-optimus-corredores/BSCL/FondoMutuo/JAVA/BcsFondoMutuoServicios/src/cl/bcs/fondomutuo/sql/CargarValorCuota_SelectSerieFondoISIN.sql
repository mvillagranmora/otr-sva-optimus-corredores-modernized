SELECT 
"IdSerieFondo", "ISIN"
FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo"
WHERE "ISIN" IN (:ISIN)
