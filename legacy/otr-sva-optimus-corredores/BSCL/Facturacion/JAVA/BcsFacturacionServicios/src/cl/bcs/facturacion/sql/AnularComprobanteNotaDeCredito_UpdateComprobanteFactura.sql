UPDATE Cmbte
SET "IdComprobanteReferencia" =0,"IdTipoComprobanteReferencia"='',"FolioComprobanteReferencia"=NULL
FROM "BSCL.Facturacion.BD::FAC.Comprobante" Cmbte
WHERE "IdComprobante" = (:IdComprobante);