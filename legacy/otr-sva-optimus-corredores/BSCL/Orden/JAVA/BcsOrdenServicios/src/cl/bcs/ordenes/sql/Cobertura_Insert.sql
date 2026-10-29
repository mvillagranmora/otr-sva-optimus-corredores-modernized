INSERT INTO #p_SimulacionCobertura
	(
	"IdReferencial",
	"IdInstrumento",
	"Cantidad",
	"PrecioMercado",
	"Monto",
	"MontoCoberturaSimular"
	)
VALUES
	(:IdReferencial,:IdInstrumento,:Cantidad,:PrecioMercado,:Monto,:MontoCoberturaSimular)
				