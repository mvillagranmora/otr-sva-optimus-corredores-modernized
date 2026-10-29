SELECT
 SAP."RutCliente"
,SAP."RegimenTributario" 
,SAP."IdContratoSAAPV" 
,SAP."FolioContratoSAAPV" 
FROM "BSCL.APV.BD.Vistas::AFI.ConsultarGestionContratosSAAPV" SAP
WHERE SAP."IdEstado" = 'VIG'
AND SAP."RutCliente"  IN (:RutCliente)