CREATE LOCAL TEMPORARY TABLE "#ModificarComisionMovimientosPorDetallePerfilCobro_DetallePerfilCobro" 
(
	"IdDetallePerfilCobros" BIGINT
	--, "IdPerfilCobros" NVARCHAR(40)
	, "IdBolsa" NVARCHAR(10)
	, "IdTipoProducto" NVARCHAR(4)
	, "IdTipoOperacion" NVARCHAR(6)
	, "IdTipoCanalIngreso" NVARCHAR(3)
	, "IdPortafolio" BIGINT
	, "IdCobro" NVARCHAR(10)
	, "IdMoneda" NVARCHAR(3)
	, "ValorCobro" DECIMAL(26,8)
	, "Borrado" NVARCHAR(1)
);
