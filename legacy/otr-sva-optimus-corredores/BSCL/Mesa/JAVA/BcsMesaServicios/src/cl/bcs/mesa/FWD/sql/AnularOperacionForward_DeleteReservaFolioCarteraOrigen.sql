DELETE FROM "BSCL.Custodia.BD::CUS.Reserva"
WHERE "FolioCarteraOrigen" IN (:IdOperacion)
  AND "IdTipoProducto" = 'MEFW';
