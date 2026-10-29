-- Currency of the detalle of the comprobantes being liquidated (the legacy query lived in the Java source)
SELECT "IdComprobante","IdMoneda" FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" WHERE "IdComprobante" in (:IdComprobante);
