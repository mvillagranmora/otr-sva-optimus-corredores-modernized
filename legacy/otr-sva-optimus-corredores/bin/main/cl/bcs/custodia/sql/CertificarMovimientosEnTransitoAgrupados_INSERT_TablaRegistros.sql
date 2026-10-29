INSERT INTO #MOVENTRANSITOAGR (
	"IdCliente",
	"DocumentoIdentidad",
	"IdTipoOperacion",
	"IdClasificacionTipoInstrumento",
	"FechaOperacion" ,
	"FechaLiquidacion" ,
	"IdCondicionLiquidacion",
	"IdConcepto",
	"IdInstrumento",
	"Nemotecnico",
	"IdMonedaInstrumento",
	"IdBoveda",
	"EsNominal"	
	)
VALUES (
	:IdCliente,
	:DocumentoIdentidad,
	:IdTipoOperacion,
	:IdClasificacionTipoInstrumento,
	TO_DATE(:FechaOperacion, 'YYYY-MM-DD'),
	TO_DATE(:FechaLiquidacion, 'YYYY-MM-DD'),
	:IdCondicionLiquidacion,
	:IdConcepto,
	:IdInstrumento,
	:Nemotecnico,
	:IdMonedaInstrumento,
	:IdBoveda,
	:EsNominal
	);