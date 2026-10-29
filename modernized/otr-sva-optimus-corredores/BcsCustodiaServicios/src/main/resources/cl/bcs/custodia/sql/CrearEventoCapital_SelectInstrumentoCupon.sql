SELECT INS."IdInstrumento", INS."Nemotecnico", MIN(CUP."Fecha") AS "FechaPagoCupon" 
FROM "BSCL.Plataforma.BD::INS.Cupon" AS CUP
    INNER JOIN  "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON (INS."Nemotecnico" = CUP."Nemotecnico")
WHERE CUP."Call" = 'S' AND INS."IdInstrumento" IN (:p_IdInstrumento) 
GROUP BY INS."IdInstrumento",INS."Nemotecnico"