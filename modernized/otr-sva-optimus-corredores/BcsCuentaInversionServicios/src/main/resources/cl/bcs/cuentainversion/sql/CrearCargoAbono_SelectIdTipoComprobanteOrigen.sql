SELECT "IdTipoComprobanteOrigen", "IdComprobanteDetalle" FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle"
WHERE "IdComprobanteDetalle" IN (:IdComprobanteDetalle);