SELECT SOL."IdSolicitudAporteRescate" AS "Valor5", SOL."FolioContratoOperacion" AS "Valor2"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL
LEFT JOIN "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" CSC 
ON SOL."FolioContratoOperacion" = CSC."FolioCSC"
WHERE SOL."TipoContratoOperacion" = 'CSC'
AND CSC."FolioCSC" IS NULL;


