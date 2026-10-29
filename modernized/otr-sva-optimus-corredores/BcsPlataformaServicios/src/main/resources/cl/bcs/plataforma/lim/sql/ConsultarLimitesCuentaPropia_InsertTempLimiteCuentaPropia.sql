INSERT INTO #ConsultarLimitesCuentaPropia_LimitesPorClasificacion
(
	"IdInterno"
	, "IdInstrumento"
	, "IdTipoInstrumento"
	, "IdTipoLimite"
	, "IdTipoOperacion"
	, "IdTipoCanalIngreso"
	, "CodigoOperador"
	, "IdMonedaOperacion"
	, "IdClasificacionTipoInstrumento"
)
VALUES
(
	:IdInterno
	, :IdInstrumento
	, :IdTipoInstrumento
	, :IdTipoLimite
	, :IdTipoOperacion
	, :IdTipoCanalIngreso
	, :CodigoOperador
	, :IdMonedaOperacion
	, :IdClasificacionTipoInstrumento
)