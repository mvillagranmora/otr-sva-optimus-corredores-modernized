SELECT 
    "IdInstrumento", 
    "Nemotecnico",
    "IdEmisor"
FROM "BSCL.Plataforma.BD::INS.Instrumento"
WHERE "Nemotecnico" = :NemoDestino
  AND "IdEstado" = 'ACT';