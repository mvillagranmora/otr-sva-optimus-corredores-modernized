CREATE LOCAL TEMPORARY TABLE #p_BloqueoProcesarReinversion (
	"IdBloqueaReinversion"   			NVARCHAR(100),
	"IdPortafolio"						BIGINT,
	"IdSerieFondo"						NVARCHAR(40),
	"IdRegimenTributario"				NVARCHAR(40),
	"IdCliente"							BIGINT,
	"NumeroCuotas"  					DECIMAL(26,8),
	"Monto" 							DECIMAL(26,8),
	"FechaSistema"      				DATE,
	"IdGrupoRegimen"					NVARCHAR(40)
);