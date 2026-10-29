SELECT SFON."IdSerieFondo" AS "Nemotecnico", SFON."IdInstrumento" 
FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FON
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SFON ON  SFON."IdFondo" = FON."IdFondo"
WHERE FON."MoneyMarket" = 'S';