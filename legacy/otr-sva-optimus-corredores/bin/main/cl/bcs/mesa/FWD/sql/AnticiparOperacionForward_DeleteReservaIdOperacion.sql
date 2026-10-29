DELETE FROM "BSCL.Custodia.BD::CUS.Reserva"
WHERE "IdOperacion" IN (:IdOperacion)
  AND "IdTipoProducto" = 'MEFW';
