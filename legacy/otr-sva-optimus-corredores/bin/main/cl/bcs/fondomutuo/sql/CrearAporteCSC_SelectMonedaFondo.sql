SELECT 
F."IdMoneda", 
SF."IdSerieFondo" 
FROM "BSCL.FondoMutuo.BD::FMM.Fondo" F
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SF ON (SF."IdFondo" = F."IdFondo")
WHERE SF."IdSerieFondo" IN (:IdSerieFondo);