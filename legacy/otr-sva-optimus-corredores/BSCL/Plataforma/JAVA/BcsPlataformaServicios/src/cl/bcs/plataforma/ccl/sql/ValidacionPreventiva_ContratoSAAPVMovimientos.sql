SELECT SOL."IdSolicitudAporteRescate" AS "Valor5", SOL."FolioContratoOperacion" AS "Valor2"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL
LEFT JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" SAAPV 
ON SOL."FolioContratoOperacion" = SAAPV."FolioContratoSAAPV"
WHERE SOL."TipoContratoOperacion" = 'SAAPV'
AND SAAPV."FolioContratoSAAPV" IS NULL;
