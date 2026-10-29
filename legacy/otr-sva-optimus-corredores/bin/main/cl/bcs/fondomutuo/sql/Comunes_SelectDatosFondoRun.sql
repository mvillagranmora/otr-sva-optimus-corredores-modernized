SELECT REPLACE(F."CodigoSVS",'-','') AS "RunFondo",S."CodigoSerie" AS "Serie",S."IdSerieFondo",S."ISIN"
FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS F
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS S ON (S."IdFondo" = F."IdFondo") 
WHERE REPLACE(F."CodigoSVS",'-','') IN (:RunFondo) AND S."CodigoSerie" IN (:SerieFondo)