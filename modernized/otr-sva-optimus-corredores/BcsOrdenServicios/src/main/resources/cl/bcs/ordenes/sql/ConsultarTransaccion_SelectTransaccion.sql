
SELECT "IdTransaccion", "FolioTransaccion", "CantidadDisponibleTransaccion", "MontoTransaccion", "ValorRescateTransaccion", "MarcaNegociacion"
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionPorAsignar"
WHERE "FolioTransaccion" IN (:folio) AND "MarcaNegociacion" IN (:marca)