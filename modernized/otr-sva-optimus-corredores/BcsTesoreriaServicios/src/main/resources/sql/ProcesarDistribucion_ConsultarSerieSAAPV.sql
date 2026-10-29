SELECT "CSAPV"."FolioContratoSAAPV", 
       "DI"."Nemotecnico", 
       ROUND("DI"."MontoPesos", 0) AS "MontoPesos",  
       "P"."CodigoPortafolio" AS "Codigo", 
       "CSAPV"."IdEstado" AS "EstadoContrato", 
       "PCS"."IdEstado" AS "EstadoPortafolio", 
       "I"."IdTipoInstrumento", 
       "P"."IdPortafolio", 
       "DI"."IdInstrumento", 
       "I"."IdEmisor",
       CASE WHEN "I"."IdTipoInstrumento" = 'FFM' THEN COALESCE("FON"."IdMoneda",'CLP') 
	      ELSE COALESCE("I"."IdMoneda",'CLP') END AS "IdMonedaInstrumento",
	    "CSAPV"."Canal",
	    "PCS"."MontoPesos" AS "MontoDistribucion"
FROM "BSCL.APV.BD::SCS.ContratoSAAPV" AS "CSAPV"
INNER JOIN "BSCL.APV.BD::SCS.DistribucionInversion" AS "DI" ON "CSAPV"."IdContratoSAAPV" = "DI"."IdContratoSAAPV"
INNER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS "PCS" ON "PCS"."IdContratoSAAPV" = "CSAPV"."IdContratoSAAPV"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS "P" ON "P"."IdPortafolio" = "PCS"."IdPortafolio"
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "I" ON "I"."IdInstrumento" = "DI"."IdInstrumento"
LEFT JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" AS "FON" ON "I"."IdInstrumento" = "FON"."IdInstrumento"
  WHERE "CSAPV"."FolioContratoSAAPV" IN (:foliosAPV)
ORDER BY "CSAPV"."FolioContratoSAAPV", "DI"."Nemotecnico"; 
