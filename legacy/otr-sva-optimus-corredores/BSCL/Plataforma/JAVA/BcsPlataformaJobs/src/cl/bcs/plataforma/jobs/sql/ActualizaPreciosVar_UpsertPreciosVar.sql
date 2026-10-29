UPSERT "BSCL.Plataforma.BD::INS.VectorPrecios" (
	"IdInstrumento",
	"FechaVector",
	"SecuenciaLista",
	"Tipo",
	"ValorPrecioBase",
	"ValorPrecioOperacionesPlazo01",
	"ValorPrecioVentaCorta02",
	"ValorOtrosPrecios03",
	"ValorOtrosPrecios04",
	"ValorOtrosPrecios05",
	"ValorPrecioAlgorithmics95",
	"ValorPrecioAlgorithmics98"
) VALUES(
	:IdInstrumento,
	:FechaVector,
	:secLista,
	:tipo,
	:valor,
	:valor01,
	:valor02,
	:valor03,
	:valor04,
	:valor05,
	:valor95,
	:valor98
) 
WHERE "IdInstrumento"=:IdInstrumento  AND "FechaVector"=:FechaVector;