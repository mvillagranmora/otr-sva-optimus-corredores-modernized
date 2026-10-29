SELECT
	EvCapCli."IdEventoCapitalCliente" 																										AS "IdGenerarMovimientoCustodia",
	CAST(CURRENT_UTCTIMESTAMP AS DATE)																										AS "FechaOperacion",
	ConCliente."IdCliente" 																													AS "IdCliente",
	EvCapCli."IdPortafolio" 																												AS "IdPortafolio",
	'SIS' 																																	AS "IdTipoCanalIngreso",

	'SUECOS' 																																AS "IdConcepto",
	'INGOSA' 																																AS "IdTipoOperacion",
	EvCapCli."IdEventoCapitalCliente" 																										AS "IdOperacion",
	EvCapCli."IdBolsa" 																														AS "IdBolsa",
	EvCapCli."IdBoveda" 																													AS "IdBoveda",
	'T0' 																																	AS "IdCondicionLiquidacion",
	'OSA N°:' ||Cast(:IdEventoCapital as nvarchar(8))|| ' ' || Trim(EvCapCli."Nemo")  || 'Fecha: ' ||CAST(EvCapCli."FechaPago" AS DATE)		AS "Glosa",
	RelInst."IdClasificacionTipoInstrumentoRelacionado" 																					AS "IdClasificacionTipoInstrumento",
	RelInst."IdTipoInstrumentoRelacionado" 																									AS "IdTipoInstrumento",
	RelInst."IdInstrumentoRelacionado" 																										AS "IdInstrumento",
	RelInst."NemoRelacionado" 																												AS "Nemotecnico",
	Instrumento."IdEmisor" 																													AS "IdEmisor",
	Instrumento."IdMoneda" 																													AS "IdMonedaInstrumento",
	EvCapCli."CantidadNueva" 																												AS "Cantidad",
	EvCap."Valor"																															AS "Precio"
FROM "BSCL.Custodia.BD::CUS.EventoCapital" 										AS EvCap
	INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente"						AS EvCapCli
		ON EvCap."IdEventoCapital" = EvCapCli."IdEventoCapital"
	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 				AS ConCliente 
		ON EvCapCli."IdPortafolio" = ConCliente."IdPortafolio"
	INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 			AS Instrumento 
		ON EvCapCli."IdInstrumento" = Instrumento."IdInstrumento"
	INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarRelacionInstrumento"	AS RelInst	
		ON EvCapCli."IdInstrumento" = RelInst."IdInstrumento"
WHERE EvCapCli."IdEventoCapital" = :IdEventoCapital
  AND EvCapCli."IdTipoEventoCapital" = 'EP'
  AND EvCapCli."CantidadNueva" > 0
  AND RelInst."IdEstado"= 'VIG'
  AND EvCapCli."TipoCartera" = 'GAR';