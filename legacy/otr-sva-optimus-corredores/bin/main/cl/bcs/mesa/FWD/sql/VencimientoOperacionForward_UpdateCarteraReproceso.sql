UPDATE "BSCL.Custodia.BD::CUS.Cartera"
SET "FechaModificacionRegistro" = :FechaModificacionRegistro,
	"EGRPrecio" = :Precio,
	"EGRMontoLiquidacion" = :MontoLiquidacion
WHERE "IdCartera" IN (:IdCartera)
  AND "IdTipoProducto" = 'MEFW';
