UPDATE "BSCL.Orden.BD::ORD.CobroOrden"
SET 
	"MontoCobroCalculado" = :MontoCobroCalculado,
	"MontoCobroCalculadoLocal" = :MontoCobroCalculadoLocal,
	"MontoCobroCalculadoLiquidacion" = :MontoCobroCalculadoLiquidacion
WHERE "IdCobroOrden" = :IdCobroOrden
AND "IdOrden" = :IdOrden ;
