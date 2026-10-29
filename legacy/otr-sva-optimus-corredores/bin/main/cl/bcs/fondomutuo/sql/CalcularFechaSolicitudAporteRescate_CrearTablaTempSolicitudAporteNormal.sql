CREATE LOCAL TEMPORARY TABLE #p_SolicitudAporteRescate(
	"IdInterno"			  		BIGINT,
	"IdSerieFondo"		  		NVARCHAR(40),
	"FechaEmision"		  		NVARCHAR(10),
	"EstadoFondo"		  		NVARCHAR(1),
	"EstadoLiquidacion"	  		NVARCHAR(1),
	"NumeroDiasRetencion" 		BIGINT,
	"NumeroValutaValorCuota"    BIGINT,
	"NumeroValutaCertificacion"	BIGINT,
	"NumeroValutaCurse"			BIGINT,
	"SolicitudBatch"			NVARCHAR(1),
	"TipoSolicitud"             NVARCHAR(3),
	"FondoExtranjero"           NVARCHAR(1),
	"IdPaisFondo"               BIGINT,
	"IdPaisEmisor"              BIGINT,
	"IdPaisMoneda"              BIGINT
)