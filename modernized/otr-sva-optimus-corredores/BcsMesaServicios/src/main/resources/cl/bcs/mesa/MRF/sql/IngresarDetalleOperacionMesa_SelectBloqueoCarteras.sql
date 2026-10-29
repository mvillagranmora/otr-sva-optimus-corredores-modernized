SELECT "IdCartera"
FROM "BSCL.Custodia.BD::CUS.Cartera"
WHERE "FolioCartera" IN (:ListaFolioCartera)
FOR UPDATE;