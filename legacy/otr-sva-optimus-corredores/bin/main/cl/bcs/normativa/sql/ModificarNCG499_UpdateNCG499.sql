UPDATE CIR
SET 
    CIR."ISIN" = :ISIN,
    CIR."FechaVencimiento" = :FechaVencimiento,
    CIR."TendenciaDeRiesgo" = :TendenciaDeRiesgo,
    CIR."ClasificacionRiesgoInstrumento" = :ClasificacionRiesgoInstrumento,
    CIR."BaseTasa" = :BaseTasa,
    CIR."Duration" = :Duration,
    CIR."CodigoPais" = :CodigoPais,
    CIR."CodigoPaisEmisionInstrumento" = :CodigoPaisEmisionInstrumento,    
   	CIR."FuentePreciosGarantia" = :FuentePreciosGarantia,
    CIR."FuentePreciosPrestamo" =  :FuentePreciosPrestamo,
    CIR."FuentePrecios" =  :FuentePrecios,     
    CIR."CantidadCustodia" = :CantidadCustodia,
    CIR."CantidadGarantia" = :CantidadGarantia,
    CIR."CantidadPrestamo" = :CantidadPrestamo,
    CIR."PrecioMercadoCustodia" = :PrecioMercadoCustodia,
    CIR."TasaMercadoCustodia" = :TasaMercadoCustodia,
    CIR."CodigoValorizacion" = :CodigoValorizacion,
    CIR."MontoMercadoCustodia" = :MontoMercadoCustodia,
    CIR."IdTipoInstrumento" = :IdTipoInstrumento,
    CIR."IdTipoInstrumentoCMF" = :IdTipoInstrumentoCMF,
    CIR."IdEstado" = CASE
                        WHEN :IdEstado = 'DES' THEN :IdEstado
                        WHEN :CantidadCustodia < CAST(0 AS DECIMAL(26,8))
                        OR :CantidadGarantia < CAST(0 AS DECIMAL(26,8))
                        OR :CantidadPrestamo < CAST(0 AS DECIMAL(26,8))
                        OR :CantidadPlazoCompra < CAST(0 AS DECIMAL(26,8))
                        OR :CantidadPlazoVenta < CAST(0 AS DECIMAL(26,8))
                        THEN 'NEG'
                        ELSE :IdEstado
                    END,
    CIR."ValorizacionPrestamo" = :ValorizacionPrestamo,
    CIR."ValorizacionGarantia" = :ValorizacionGarantia,
    CIR."CantidadPlazoCompra" = :CantidadPlazoCompra,
    CIR."ValorizacionPlazoCompra" = :ValorizacionPlazoCompra,
    CIR."CantidadPlazoVenta" = :CantidadPlazoVenta,
    CIR."ValorizacionPlazoVenta" = :ValorizacionPlazoVenta,
    CIR."ClasificacionInstrumentoCustodia" = :ClasificacionInstrumentoCustodia,
    CIR."ClasificacionInstrumentoGarantia" = :ClasificacionInstrumentoGarantia,
    CIR."ClasificacionInstrumentoPrestamo" = :ClasificacionInstrumentoPrestamo,
    CIR."ClasificacionInstrumentoPlazo" = :ClasificacionInstrumentoPlazo,
    CIR."FuentePreciosPlazo" = :FuentePreciosPlazo,
    CIR."IdTipoInstrumentoPlazoCompra" = :IdTipoInstrumentoPlazoCompra,
    CIR."IdTipoInstrumentoPlazoVenta" = :IdTipoInstrumentoPlazoVenta
FROM "BSCL.Custodia.BD::CUS.CircularCarteraCMF" CIR
WHERE
    CIR."Nemotecnico" = :Nemotecnico
    AND CIR."Fecha" = :Fecha
    AND CIR."IdEmisor" = :IdEmisor
    AND CIR."IdOrigen" = :IdOrigen;