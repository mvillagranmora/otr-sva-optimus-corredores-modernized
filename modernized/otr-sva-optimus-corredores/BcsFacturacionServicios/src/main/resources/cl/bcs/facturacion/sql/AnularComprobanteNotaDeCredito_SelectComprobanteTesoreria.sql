SELECT "IdComprobante" as "IdComprobanteTesoreria"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" WHERE "IdEstado"<>'ANU'
AND "IdComprobanteOrigen" = :idComprobante