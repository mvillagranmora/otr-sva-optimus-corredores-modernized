SELECT "FolioCartera"
FROM "BSCL.Custodia.BD::CUS.Cartera"
WHERE "FolioCartera" IN (:SecuenciaCartera) AND "IdEstado" = 'VIG'