SELECT 
DISTINCT
"IdRegimenFondoMutuoOrigen" || '_' || "IdRegimenFondoMutuoDestino" AS "Id"
,"IdRegimenFondoMutuoOrigen"
,"IdRegimenFondoMutuoDestino"
FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionRegimenFondoMutuo"
WHERE "IdRegimenFondoMutuoOrigen" IN (:IdRegimenFondoMutuoOrigen)