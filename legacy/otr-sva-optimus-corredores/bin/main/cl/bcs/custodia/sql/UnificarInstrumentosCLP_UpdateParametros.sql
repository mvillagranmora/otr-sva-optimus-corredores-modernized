UPDATE "BSCL.Plataforma.BD::INS.Instrumento"
SET 
    "IdInstrumentoRaiz" = ?,
    "IdEmisor" = ?,
    "IdCondicionLiquidacion" = ?
WHERE "Nemotecnico" = ?
  AND (
      IFNULL("IdInstrumentoRaiz", 0) <> ? OR
      IFNULL("IdEmisor", '') <> ? OR
      IFNULL("IdCondicionLiquidacion", '') <> ?
  )