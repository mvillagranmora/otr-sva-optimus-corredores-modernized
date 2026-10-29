UPDATE "BSCL.Custodia.BD::CUS.Cartera"
SET "EGRIdMonedaLiquidacion" = :IdMonedaLiquidacion
WHERE "IdCartera" IN (:IdCartera)
  AND "IdTipoProducto" = 'MEFW';
