INSERT INTO #p_BloqueoProcesarReinversion (
	"IdBloqueaReinversion",
	"IdPortafolio",
	"IdSerieFondo",
	"IdRegimenTributario",
	"IdCliente",
	"NumeroCuotas",
	"Monto",
	"FechaSistema",
	"IdGrupoRegimen"
)
VALUES (
	:IdBloqueaReinversion, 
	:IdPortafolio, 
	:IdSerieFondo,
	:IdRegimenTributario,
	:IdCliente,
	:NumeroCuotas,
	:Monto,
	:FechaSistema,
	:IdGrupoRegimen
);