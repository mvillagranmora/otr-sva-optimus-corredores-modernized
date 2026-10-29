SELECT NEG."Nemotecnico" AS "NemoNegociable", INS."Nemotecnico" AS "NemoRaiz", NEG."IdInstrumentoRaiz", NEG."IdMoneda",
CASE WHEN COALESCE(NEG."IdCondicionLiquidacion",'') = '' THEN ''
  WHEN COALESCE(NEG."IdCondicionLiquidacion",'') = 'T2' THEN ''
  WHEN COALESCE(NEG."IdCondicionLiquidacion",'') = 'T1' THEN 'PM'
  WHEN COALESCE(NEG."IdCondicionLiquidacion",'') = 'T0' THEN 'PH' 
END AS "IdCondicionLiquidacion"
FROM "BSCL.Plataforma.BD::INS.Instrumento" NEG 
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON NEG."IdInstrumentoRaiz"=INS."IdInstrumento"
WHERE NEG."Nemotecnico" IN (:nemos);