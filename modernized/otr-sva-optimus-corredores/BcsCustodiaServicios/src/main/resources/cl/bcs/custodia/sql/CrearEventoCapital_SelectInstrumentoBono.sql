SELECT INS."IdInstrumento",INS."Nemotecnico", BON."CallConfirmado", BON."CuponeraVariable"
FROM "BSCL.Plataforma.BD::INS.Instrumento" AS INS
    INNER JOIN "BSCL.Plataforma.BD::INS.InstrumentoBono" AS BON ON (BON."IdInstrumento" = INS."IdInstrumento")
WHERE INS."IdInstrumento" IN (:p_IdInstrumento)