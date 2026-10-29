SELECT "Ins"."IdInstrumento" AS "IdInstrumentoAnterior", 
       "InsCam"."IdInstrumento" AS "IdInstrumentoNuevo",
       "Ins"."NemotecnicoCambio" AS "NemoNuevo",
       "InsCam"."IdEmisor" AS "Emisor"
FROM  "BSCL.Plataforma.BD::INS.Instrumento" "Ins"
JOIN  "BSCL.Plataforma.BD::INS.Instrumento" "InsCam" ON ("InsCam"."Nemotecnico" = "Ins"."NemotecnicoCambio")
WHERE "Ins"."FechaHastaAcreditado" = :FechaSistema;