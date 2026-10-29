UPDATE "BSCL.Plataforma.BD::COM.ValorCobro"
SET
	"DescripcionValorCobro" = :DescripcionValorCobro,
	"IdTipoProducto" = :IdTipoProducto,
	"IdCobro" = :IdCobro,
	"ValorCobro" = :ValorCobro,
	"ValorTopeMinimoOrden" = :ValorTopeMinimoOrden,
	"IdMoneda" = :IdMoneda,
	"IdEstado" = :IdEstado,
	"IdEmpresa" = :IdEmpresa
WHERE "IdValorCobro" = :IdValorCobro
