INSERT INTO #p_DatosFormaPago (
	"NumeroOperacion" 
	, "IdTipoProducto"
	, "IdTipoMovimiento"
	, "IdMedioDePago"
	, "NombreMedioDePago"
	, "IdModalidadPago"
	, "IdMoneda"
	, "Monto"
	, "FechaIngresoRegistro"
	, "FechaCondicionLiquidacion"
	, "AfectarTesoreria"
) VALUES (
	:NumeroOperacion
	, :IdTipoProducto
	, :IdTipoMovimiento
	, :IdMedioDePago
	, :NombreMedioDePago
	, :IdModalidadPago
	, :IdMoneda
	, :Monto
	, :FechaIngresoRegistro
	, :FechaCondicionLiquidacion
	, :AfectarTesoreria
)