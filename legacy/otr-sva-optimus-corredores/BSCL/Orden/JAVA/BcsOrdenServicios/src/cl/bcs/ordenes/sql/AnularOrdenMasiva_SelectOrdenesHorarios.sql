SELECT ord."IdOrden" 
,TO_NVARCHAR(COALESCE(ordhorario."HorarioPreMarketInicio",'00:00:00')) AS "HorarioPreMarketInicio"
,TO_NVARCHAR(COALESCE(ordhorario."HorarioMarketFin",'00:00:00')) AS "HorarioMarketFin"
,ordhorario."AplazaOrden" AS "AplazaOrdenPorHorarioNegociacion"
,ordhorario."ValidaHorario"
,ordhorario."IdTipoCanalDistribucion"
FROM "BSCL.Orden.BD::ORD.Orden" AS ord
INNER JOIN #entrada_anular_ordenes AS tmp ON tmp."IdOrden" = ord."IdOrden"
INNER JOIN "BSCL.Orden.BD::ORD.ConfiguracionHorarioDistribucion" ordhorario on 
			ordhorario."IdTipoCanalDistribucion" = ord."IdTipoCanalDistribucion" and
			ordhorario."IdTipoCanalIngreso" = ord."IdTipoCanalIngreso" and
			ordhorario."IdTipoInstrumento" = ord."IdTipoInstrumento" and
			ordhorario."IdBolsa" = ord."IdBolsa" and
			ordhorario."IdCondicionDeLiquidacion" = 'T0';