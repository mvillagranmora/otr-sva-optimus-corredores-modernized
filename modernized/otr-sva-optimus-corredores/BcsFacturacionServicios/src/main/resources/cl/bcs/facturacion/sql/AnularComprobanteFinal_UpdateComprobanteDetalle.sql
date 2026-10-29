UPDATE CmbteDet
SET "IdEstadoComprobanteDetalle" = 'ANU'
FROM "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" CmbteDet
WHERE "IdComprobante" = (:IdComprobante);        
