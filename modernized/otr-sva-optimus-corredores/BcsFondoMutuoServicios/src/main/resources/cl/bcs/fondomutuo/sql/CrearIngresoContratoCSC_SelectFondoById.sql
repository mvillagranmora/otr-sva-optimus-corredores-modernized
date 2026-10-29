SELECT 
	SFON."IdSerieFondo",
	FON."IdMoneda"	
FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FON
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SFON ON (SFON."IdFondo" = FON."IdFondo")
WHERE SFON."IdSerieFondo" IN (:IdSerieFondo);