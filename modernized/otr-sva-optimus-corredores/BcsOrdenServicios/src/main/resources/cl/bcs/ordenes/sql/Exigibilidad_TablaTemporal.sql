CREATE LOCAL TEMPORARY TABLE #p_TableExigibilidadSimulacion
(
"IdInterno"			BIGINT,
"IdTipoInstrumento"	NVARCHAR(3),
"IdInstrumento" 	BIGINT,				--ID SUBYACENTE, FUTUROS???
"Cantidad" 			DECIMAL(26,8),
"Precio" 			DECIMAL(26,8),
"Monto" 			DECIMAL(26,8),
"IdBolsa" 			NVARCHAR(3)
)