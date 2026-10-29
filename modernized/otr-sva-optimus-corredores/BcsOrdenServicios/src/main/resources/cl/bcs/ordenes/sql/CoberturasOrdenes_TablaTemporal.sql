CREATE LOCAL TEMPORARY TABLE #p_SimulacionCobertura
(
"IdReferencial"     BIGINT, 
"IdInstrumento" 	BIGINT, 
"Cantidad" 			DECIMAL(26,8),
"PrecioMercado" 	DECIMAL(26,8),
"Monto" 			DECIMAL(26,8),
"MontoCoberturaSimular"  	DECIMAL(26,8)
)