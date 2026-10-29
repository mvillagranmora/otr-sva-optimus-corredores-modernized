select "FolioCartera"
FROM "BSCL.Custodia.BD::CUS.Cartera"
WHERE "FolioCartera" IN (:FolioCartera) and "IdEstado" = 'ANU'; 
