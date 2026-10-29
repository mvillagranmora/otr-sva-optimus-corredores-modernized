SELECT 
REL."IdSerieFondo",
REG."IdRegimenFondoMutuo",
REG."Descripcion" AS "DescripcionRegimen",
CASE
    WHEN FON."FondoExtranjero" = 'S' THEN 'N'
    WHEN FON."FondoExtranjero" = 'N' THEN REG."RescateConMandato"
END AS "PermiteMLT"
FROM "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" REL
INNER JOIN  "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" REG ON REL."IdRegimenFondoMutuo"=REG."IdRegimenFondoMutuo"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SER ON REL."IdSerieFondo"  = SER."IdSerieFondo"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" FON ON SER."IdFondo" = FON."IdFondo"
WHERE REL."IdSerieFondo" IN (:IdSerieFondo)
	AND REL."IdEstado" = 'ACT'
ORDER BY "IdRegimenFondoMutuo" ASC