
SELECT
	--DATOS RECIVIDOS
	"FolioPrestamo","FolioEmprestito","DevolverCustodia","IdEmpleadoDigitador","FechaAnticipo","PlazoAlVencimiento",
	"UtilizaDatosEnviados",
	--EMPRESTITO OPERACION
    "CobraPrimaAlInicio","IdCliente","IdPortafolio","FechaEmprestito","FechaVencimientoEmprestito","IdTipoProducto","IdTipoOperacion",
	"IdPais","IdMonedaSistema","IdMonedaLiquidacion","IdCondicionLiquidacion","FechaLiquidacion","IdTipoComprobante",
	"FolioComprobante","FechaComprobante","IdAgente","IdSucursal","IdBolsa","IdCartera","FolioCartera",
	--INSTRUMENTO
	"IdTipoInstrumento","IdInstrumento","IdEmisor","IdBoveda","IdTipoReajuste",
	--MONTO
	"CantidadAnticipo","PrecioInstrumentoAnticipo","MontoInstrumentoAnticipo","TasaPremioAnticipo","MontoPremioAnticipo",
	"MontoAPlazoAnticipo","MontoPremio",
	--SUBYACENTE
	"IdClasificacionTipoInstrumentoSubyacente","IdTipoInstrumentoSubyacente","IdInstrumentoSubyacente","IdEmisorSubyacente",
	"IdMonedaSubyacente",
	--PRESTAMO RELACIONADO
	"IdClientePrestamo","IdPortafolioPrestamo"
FROM "BSCL.Custodia.BD::GBL.AnticiparEmprestito_Proceso_Emprestitos";      