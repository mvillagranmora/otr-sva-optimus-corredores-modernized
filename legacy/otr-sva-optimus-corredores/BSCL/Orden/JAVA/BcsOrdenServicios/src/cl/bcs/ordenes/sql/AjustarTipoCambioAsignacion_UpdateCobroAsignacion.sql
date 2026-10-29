UPDATE "BSCL.Orden.BD::ORD.CobroAsignacion"
SET 
	"MontoCobroCalculado" = :MontoCobroCalculado,
	"MontoCobroCalculadoLocal" = :MontoCobroCalculadoLocal,
	"MontoCobroCalculadoLiquidacion" = :MontoCobroCalculadoLiquidacion
WHERE "IdCobroAsignacion" = :IdCobroAsignacion
AND "IdAsignacion" = :IdAsignacion ;