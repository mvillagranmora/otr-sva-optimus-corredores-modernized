CREATE LOCAL TEMPORARY TABLE #p_DatosOperacion (
	"NumeroOperacion" NVARCHAR(64)
	, "IdInstrumento" BIGINT
	, "IdTipoOperacion" NVARCHAR(6)
	, "IdCliente" BIGINT
	, "IdPortafolio" BIGINT
	, "FechaOperacion" SECONDDATE
	, "IdMonedaPrincipal" NVARCHAR(3)
	, "IdMonedaSecundaria" NVARCHAR(3)
	, "MontoMonedaPrincipal" DECIMAL(26, 8)
	, "MontoMonedaSecundaria" DECIMAL(26, 8)
	, "TipoCambioCierre" DECIMAL(26, 8)
	, "TipoCambioCierreForward" DECIMAL(26, 8)
	, "ParidadCierre" DECIMAL(26, 8)
	, "ParidadCierreForward" DECIMAL(26, 8)
	, "TipoCambioCosto" DECIMAL(26, 8)
	, "ParidadCosto" DECIMAL(26, 8)
	, "MontoEquivalente" DECIMAL(26, 8)
	, "MontoRentabilidad" DECIMAL(26, 8)
	, "MontoRentabilidadCanal" DECIMAL(26, 8)
	, "Margen" DECIMAL(26, 8)
	, "Utilidad" DECIMAL(26, 8)
	, "PuntaCompra" DECIMAL(26, 8)
	, "PuntaVenta" DECIMAL(26, 8)
	, "SolicitudTipoCambio" NVARCHAR(1)
	, "IdMesa" BIGINT
	, "IdModulo" NVARCHAR(3)
	, "IdTipoDolar" BIGINT
	, "TipoComprobanteFacturacion" NVARCHAR(3)
	, "IdAgente" BIGINT
	, "BenOur" NVARCHAR(3)
	, "EquivalenteConGasto" DECIMAL(26, 8)
	, "TipoCambioCierreConGasto" DECIMAL(26, 8)
	, "Observacion" NVARCHAR(520)
	, "CodigoComercio" BIGINT
	, "FolioPlanilla" BIGINT
	, "FolioInterfaz" NVARCHAR(50)
	, "OperacionFicticia" NVARCHAR(1)
	, "IdTipoCanalIngreso" NVARCHAR(3)
	, "IdDatatec" NVARCHAR(40)
	, "Estado" NVARCHAR(3)
)