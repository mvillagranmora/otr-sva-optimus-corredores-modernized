SELECT "IdSerieFondo","CodigoSVS","CodigoSerie" FROM 
"BSCL.FondoMutuo.BD::FMM.Fondo" AS F
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" S ON (F."IdFondo" = S."IdFondo")
WHERE REPLACE(REPLACE(F."CodigoSVS",'-', ''), '.', '') = :IdCodigoFondo AND "CodigoSerie" = :IdSerieFondo