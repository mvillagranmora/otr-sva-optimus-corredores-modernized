CREATE LOCAL TEMPORARY TABLE #IngresoPrecioParidadesSpot_p_DatosParidadYTipoCambioValorPorDefecto (
	"IdParidadYTipoCambioValorPorDefecto"		BIGINT,
	"IdMoneda"					NVARCHAR(3)
	,"NombreMoneda"				NVARCHAR(100)
	,"ParidadMercado"			DECIMAL(26,8)
	,"ParidadBID"				DECIMAL(26,8)
	,"ParidadASK" 				DECIMAL(26,8)
	,"LimiteSuperiorParidad"	DECIMAL(26,8)
	,"LimiteInferiorParidad"	DECIMAL(26,8)
	,"ParidadSBIF"				DECIMAL(26,8)
	,"TipoCambioMercado"		DECIMAL(26,8)
	,"LimiteSuperiorTipoCambio"	DECIMAL(26,8)
	,"LimiteInferiorTipoCambio"	DECIMAL(26,8)									
	,"PrecioSpotCompra" 		DECIMAL(26,8)
	,"PrecioSpotVenta"			DECIMAL(26,8)
	,"PrecioSpotPromedio"		DECIMAL(26,8)
);

