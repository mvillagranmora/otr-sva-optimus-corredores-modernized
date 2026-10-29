SELECT "IdSerieFondo",REPLACE(REPLACE(F."CodigoSVS",'-', ''), '.', '') AS "CodigoSVS","CodigoSerie" FROM 
"BSCL.FondoMutuo.BD::FMM.Fondo" AS F
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" S ON (F."IdFondo" = S."IdFondo")
WHERE REPLACE(REPLACE(F."CodigoSVS",'-', ''), '.', '') IN (:IdCodigoFondo) AND "CodigoSerie" IN (:CodigoSerie)