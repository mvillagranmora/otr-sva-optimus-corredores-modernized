SELECT 
DISTINCT 
"IdRegimenFondoMutuo" || '_' || "IdPropositoPortafolio" AS "Id"
,"IdRegimenFondoMutuo"
,"IdPropositoPortafolio"
FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionPropositoPortafolio"
WHERE "IdRegimenFondoMutuo" IN (:IdRegimenFondoMutuo)