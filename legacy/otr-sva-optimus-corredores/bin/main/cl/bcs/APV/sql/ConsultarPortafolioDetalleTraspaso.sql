SELECT 
 DET."IdDetalleTraspaso"
,DET."IdPortafolio"
,POR."CodigoPortafolio"
,POC."IdContratoSAAPV"
,CON."FolioContratoSAAPV"
,DET."IdPlanTipoInversion" 
,DET."IdRegimenTributario" 
,DET."IdProductoAgrupador" 
,DET."MontoPeso" 
,DET."MontoUF" 
FROM "BSCL.APV.BD::TRA.DetalleTraspaso" AS DET
INNER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS POC ON DET."IdPortafolio" = POC."IdPortafolio"
INNER JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" AS CON ON POC."IdContratoSAAPV" = CON."IdContratoSAAPV"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (POR."IdPortafolio" = POC."IdPortafolio")
WHERE DET."IdTraspaso" = :IdTraspaso 
	AND CON."IdContratoSAAPV" = :IdContratoSAAPV 
	AND DET."IdEstado" = 'VIG' 
	AND DET."EsReapertura" = 'N';