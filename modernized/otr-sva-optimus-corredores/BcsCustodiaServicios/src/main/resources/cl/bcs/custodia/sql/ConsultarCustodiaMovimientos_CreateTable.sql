CREATE LOCAL TEMPORARY TABLE #p_VariablesProceso(
	"IdCliente"							BIGINT,
	"IdPortafolio"						BIGINT,
	"FolioCartera" 						BIGINT,
	"IdClasificacionTipoInstrumento" 	NVARCHAR(3),
	"IdTipoInstrumento" 				NVARCHAR(3),
	"IdInstrumento" 					BIGINT
)