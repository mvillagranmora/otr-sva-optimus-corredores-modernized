CREATE LOCAL TEMPORARY TABLE #ConsultarLimitesCuentaPropia_LimitesPorClasificacion
(
	"IdInterno"                        BIGINT NOT NULL
	, "IdInstrumento"                  BIGINT NULL
	, "IdTipoInstrumento"              NVARCHAR(3) NULL
	, "IdTipoLimite"                   NVARCHAR(40) NULL
	, "IdTipoOperacion"                NVARCHAR(6) NULL
	, "IdTipoCanalIngreso"             NVARCHAR(3) NULL
	, "CodigoOperador"                 NVARCHAR(40) NULL
	, "IdMonedaOperacion"              NVARCHAR(3) NULL
	, "IdClasificacionTipoInstrumento" NVARCHAR(3) NULL
	, "IdEmisor"                       NVARCHAR(40) NULL
)