UPDATE  "BSCL.Mesa.BD::MES.Operacion" 
SET     "FolioCartera" = :FolioCartera
WHERE "IdOperacion" IN (:IdOperacion);