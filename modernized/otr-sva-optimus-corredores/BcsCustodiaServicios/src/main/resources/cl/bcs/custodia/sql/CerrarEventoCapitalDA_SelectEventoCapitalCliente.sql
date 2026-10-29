SELECT 
EvCapCli."IdEventoCapital"
,EvCapCli."IdEventoCapitalCliente"
,ConPortafolio."IdCliente"
,EvCapCli."IdPortafolio"
,EvCapCli."IdInstrumento"
,EvCapCli."IdMoneda"
,EvCapCli."TipoCartera"
,EvCapCli."IdBoveda"
,EvCapCli."IdBolsa"
,Instrumento."IdEmisor"
,EvCapCli."CantidadNueva"
,Instrumento."Nemotecnico" AS "Nemo"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EvCapCli
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 				AS ConPortafolio
	ON EvCapCli."IdPortafolio" = ConPortafolio."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 				AS ConCliente
	ON ConPortafolio."IdCliente" = ConCliente."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"				AS Instrumento
	ON EvCapCli."IdInstrumento" = Instrumento."IdInstrumento"
WHERE EvCapCli."IdEventoCapital" = :IdEventoCapital
  AND (EvCapCli."CantidadNueva" > 0 OR EvCapCli."TipoCartera" = 'NEG')
  AND EvCapCli."TipoSocioNegocio" = 'CLI';