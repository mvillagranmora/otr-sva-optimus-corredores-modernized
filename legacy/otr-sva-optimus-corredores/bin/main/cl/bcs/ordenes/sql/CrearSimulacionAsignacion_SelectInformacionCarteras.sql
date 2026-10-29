SELECT "FolioCartera","IdBoveda" FROM "BSCL.Custodia.BD::CUS.Cartera"
WHERE "FolioCartera" IN (:foliosCartera)
AND "IdEstado" = 'VIG';