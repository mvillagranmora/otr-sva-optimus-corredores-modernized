CREATE LOCAL TEMPORARY TABLE #p_DatosFormaPago (
	"NumeroOperacion" NVARCHAR(64)
	, "IdTipoProducto" NVARCHAR(4)
	, "IdTipoMovimiento" NVARCHAR(6)
	, "IdMedioDePago" NVARCHAR(3)
	, "NombreMedioDePago" NVARCHAR(100)
	, "IdModalidadPago" NVARCHAR(6)
	, "IdMoneda" NVARCHAR(3)
	, "Monto" DECIMAL(26, 8)
	, "FechaIngresoRegistro" NVARCHAR(10)
	, "FechaCondicionLiquidacion" SECONDDATE
	, "AfectarTesoreria" NVARCHAR(1)
)