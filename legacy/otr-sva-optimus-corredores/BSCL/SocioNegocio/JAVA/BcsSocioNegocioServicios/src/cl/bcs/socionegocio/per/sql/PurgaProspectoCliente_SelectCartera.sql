SELECT DISTINCT "IdPortafolio", "FolioCartera" 
FROM "BSCL.Custodia.BD::CUS.Cartera" 
WHERE "IdPortafolio" IN (:IdsPortafolio);