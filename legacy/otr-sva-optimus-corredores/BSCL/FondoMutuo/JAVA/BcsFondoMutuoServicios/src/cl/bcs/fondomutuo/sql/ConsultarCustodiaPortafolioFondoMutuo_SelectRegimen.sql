SELECT 
Regimen."IdSerieFondo"
, RegimenFM."RescateConMandato"
, RegimenFM."AceptaMandatoEnReinversion"
FROM "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" AS Regimen 
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS RegimenFM ON RegimenFM."IdRegimenFondoMutuo" = Regimen."IdRegimenFondoMutuo"
WHERE Regimen."IdSerieFondo" IN (:IdSerieFondo) AND Regimen."IdEstado"='ACT';