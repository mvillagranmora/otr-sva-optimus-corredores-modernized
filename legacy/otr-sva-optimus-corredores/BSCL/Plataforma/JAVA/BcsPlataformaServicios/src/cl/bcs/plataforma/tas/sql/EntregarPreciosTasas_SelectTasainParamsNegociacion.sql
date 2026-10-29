   SELECT distinct
    INS."IdInstrumento",
    INS."IdCondicionLiquidacion",
    PF_TINB."Valor" AS "TasaIntradayBase360",
    PF_ALOF."Valor" AS "AperturaBCS",
    INS."IdInstrumentoRaiz" 
FROM 
    "BSCL.Plataforma.BD::INS.Instrumento" INS
LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PF_TINB
    ON PF_TINB."IdParametroFinanciero" = 'TINB'
    AND PF_TINB."Fecha" IN (:fechaConsulta)
LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PF_ALOF
    ON PF_ALOF."IdParametroFinanciero" = 'ALOF'
    AND PF_ALOF."Fecha" IN (:fechaConsulta)
WHERE 
    INS."IdInstrumento" IN (:idsInstrumento)
    AND INS."IdInstrumentoRaiz" != 0; 