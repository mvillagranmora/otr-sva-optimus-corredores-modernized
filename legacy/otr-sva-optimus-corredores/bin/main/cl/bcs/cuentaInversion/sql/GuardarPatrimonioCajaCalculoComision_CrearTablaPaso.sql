CREATE LOCAL TEMPORARY TABLE #PatrimonioCaja_CobroComisionTablaPaso (
	"IdCuentaInversion" BIGINT ,
	"IdPortafolio" BIGINT,
	"IdMoneda" NVARCHAR(3),
	"MontoPatrimonioAfectoComision" DECIMAL, 
	"FechaPatrimonio" DATE
);