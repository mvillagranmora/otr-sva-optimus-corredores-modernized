UPDATE "BSCL.Orden.BD::ORD.Asignacion"
SET 
	"MontoAsignadoLiquidacion" = :MontoAsignadoLiquidacion,
	"MontoAsignadoLocal" = :MontoAsignadoLocal,
	"TipoCambio" = :TipoCambio
WHERE "IdAsignacion" = :IdAsignacion ;