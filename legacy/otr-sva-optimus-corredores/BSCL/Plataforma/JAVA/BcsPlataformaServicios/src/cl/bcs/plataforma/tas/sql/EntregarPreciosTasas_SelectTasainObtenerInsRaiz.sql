SELECT 
    INS."IdInstrumento",
    INS."IdInstrumentoRaiz",
    INS."Nemotecnico"
FROM 
    "BSCL.Plataforma.BD::INS.Instrumento" INS
WHERE 
    INS."IdInstrumento" IN (:idsInstrumento);
    