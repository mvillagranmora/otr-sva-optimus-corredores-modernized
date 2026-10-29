UPDATE NCG
  SET
    NCG."ValorMercadoCustodia"      = CASE WHEN :Situacion = 'CUSTODIA' THEN (CASE WHEN :ValMontoMonedaInstrumento = 0 THEN NCG."ValorMercadoCustodia" ELSE :ValMontoMonedaInstrumento END) ELSE NCG."ValorMercadoCustodia" END
    ,NCG."MontoMercadoCustodia"     = CASE WHEN :Situacion = 'CUSTODIA' THEN (CASE WHEN :ValMontoMonedaInstrumento = 0 THEN NCG."MontoMercadoCustodia" ELSE :ValMontoMonedaInstrumento END) ELSE NCG."MontoMercadoCustodia" END
    ,NCG."ValorizacionGarantia"     = CASE WHEN :Situacion = 'GARANTIA' THEN (CASE WHEN :ValMontoMonedaInstrumento = 0 THEN NCG."ValorizacionGarantia" ELSE :ValMontoMonedaInstrumento END) ELSE NCG."ValorizacionGarantia" END
    ,NCG."ValorizacionPrestamo"     = CASE WHEN :Situacion = 'PRESTAMO' THEN (CASE WHEN :ValMontoMonedaInstrumento = 0 THEN NCG."ValorizacionPrestamo" ELSE :ValMontoMonedaInstrumento END) ELSE NCG."ValorizacionPrestamo" END
    ,NCG."ValorizacionPlazoVenta"   = CASE WHEN :Situacion = 'PLAZOVEN' THEN (CASE WHEN :ValMontoMonedaInstrumento = 0 THEN NCG."ValorizacionPlazoVenta" ELSE :ValMontoMonedaInstrumento END) ELSE NCG."ValorizacionPlazoVenta" END
    ,NCG."ValorizacionPlazoCompra"  = CASE WHEN :Situacion = 'PLAZOCOM' THEN (CASE WHEN :ValMontoMonedaInstrumento = 0 THEN NCG."ValorizacionPlazoCompra" ELSE :ValMontoMonedaInstrumento END) ELSE NCG."ValorizacionPlazoCompra" END
  	,NCG."IndicadorValorizado"		= CASE WHEN LENGTH(TRIM(:MensajeErrorFuente)) > 0 THEN 'E' ELSE 'S' END 
	,NCG."ValResultado"			  	= CASE WHEN :ValMontoMonedaInstrumento = 0 AND LENGTH(TRIM(:MensajeErrorFuente)) = 0 THEN 'WARNING: WS RETURN 0'
	                                       WHEN LENGTH(TRIM(:MensajeErrorFuente)) > 0 THEN RPAD('ERROR: ' || TRIM(:MensajeErrorFuente), 399, '')
	                                	   ELSE 'OK' END  
FROM "BSCL.Custodia.BD::CUS.CircularCarteraCMF" AS NCG
WHERE NCG."IdCircularCartera" IN (:IdValorizacion)
AND NCG."IdEstado" NOT IN ('NEG');