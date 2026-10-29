UPDATE "BSCL.Orden.BD::ORD.Orden"
SET 
	"MontoLiquidacion" = :MontoLiquidacion,
	"MontoLocal" = :MontoLocal,
	"FechaModificacion" = :FechaModificacion
WHERE "IdOrden" = :IdOrden ;