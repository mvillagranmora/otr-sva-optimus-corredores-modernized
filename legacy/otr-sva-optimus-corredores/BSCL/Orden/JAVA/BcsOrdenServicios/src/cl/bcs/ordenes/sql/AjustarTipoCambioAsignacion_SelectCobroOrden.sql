SELECT 
	"IdCobroOrden",
	"IdOrden",
	"IdCobro",
	"IdMoneda",
	"FormaCobro",
	"ValorCobroAplicado",
	"MontoCobroCalculado",
	"MontoCobroCalculadoLocal",
	"MontoCobroCalculadoLiquidacion",
	"CodigoCobroClasificacion",
	"ValorCobroPerfil",
	"MontoMinimo",
	"PermiteCambiarValor"
FROM "BSCL.Orden.BD::ORD.CobroOrden"
WHERE "IdOrden" IN (:IdsOrden)
AND "FormaCobro" = 'V'