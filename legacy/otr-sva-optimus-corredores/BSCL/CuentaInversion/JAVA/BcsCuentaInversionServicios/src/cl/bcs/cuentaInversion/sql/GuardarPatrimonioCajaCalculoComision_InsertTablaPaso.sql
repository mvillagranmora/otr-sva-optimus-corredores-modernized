INSERT INTO #PatrimonioCaja_CobroComisionTablaPaso (
	"IdCuentaInversion",
	"IdPortafolio",
	"IdMoneda",
	"MontoPatrimonioAfectoComision", 
	"FechaPatrimonio"
) VALUES (
	:IdCuentaInversion,
	:IdPortafolio,
	:IdMoneda,
	COALESCE(:MontoPatrimonioAfectoComision, 0.0),
	:FechaPatrimonio
);