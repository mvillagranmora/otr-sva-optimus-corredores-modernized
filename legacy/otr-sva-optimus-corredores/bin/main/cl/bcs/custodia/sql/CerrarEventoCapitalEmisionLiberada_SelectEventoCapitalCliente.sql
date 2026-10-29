
SELECT
	EvCapCli."IdEventoCapital",
	EvCapCli."IdEventoCapitalCliente",	
	EvCapCli."IdClasificacionTipoInstrumento",
	EvCapCli."IdTipoInstrumento",
	EvCapCli."IdInstrumento",
	EvCapCli."IdPortafolio",
	EvCapCli."IdBolsa",
	EvCapCli."CantidadNueva",
	EvCapCli."CantidadDerechoArchivo",
	EvCapCli."IdMoneda",
	EvCapCli."IdBoveda",
	EvCapCli."TipoCartera",
	EvCapCli."Nemo",	
	EvCapCli."FechaPago",	
	Portafolio."IdCliente"

FROM        "BSCL.Custodia.BD::CUS.EventoCapitalCliente" 	AS EvCapCli
INNER JOIN  "BSCL.Custodia.BD::CUS.EventoCapital"			AS EvCap ON (EvCap."IdEventoCapital" = EvCapCli."IdEventoCapital")
INNER JOIN  "BSCL.Portafolio.BD::POR.Portafolio" 			AS Portafolio ON (EvCapCli."IdPortafolio" = Portafolio."IdPortafolio")

WHERE EvCap."IdEventoCapital" = :IdEventoCapital
  AND EvCap."IdEmpresa" = :IdEmpresa
  AND EvCap."IdTipoEventoCapital" = 'EL'
  AND EvCap."IdEstado" IN ('ACT','PRO', 'NOT')
  AND (EvCapCli."CantidadNueva" > 0 OR EvCapCli."TipoCartera" = 'NEG')
  AND EvCapCli."TipoSocioNegocio" = 'CLI';
  
  