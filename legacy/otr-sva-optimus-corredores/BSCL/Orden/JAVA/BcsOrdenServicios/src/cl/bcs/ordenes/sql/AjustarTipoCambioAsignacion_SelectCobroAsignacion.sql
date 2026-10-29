SELECT 
	"IdCobroAsignacion",
	"IdAsignacion",
	"IdCobro",
	"IdMoneda",
	"FormaCobro",
	"ValorCobro",
	"ValorCobroAplicado",
	"MontoCobroCalculado",
	"MontoCobroCalculadoLocal",
	"MontoCobroCalculadoLiquidacion",
	"CodigoCobroClasificacion",
	"CodigoCobroClasificacionBase"
FROM "BSCL.Orden.BD::ORD.CobroAsignacion"
WHERE "IdAsignacion" IN (:IdsAsignacion)
AND "FormaCobro" = 'V'