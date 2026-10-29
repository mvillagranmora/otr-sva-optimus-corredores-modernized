UPDATE "BSCL.Custodia.BD::CUS.Cartera"
SET "INGPrecioAPlazo" = 
    CASE 
        WHEN "IdTipoInstrumento" = 'PRT' THEN :INGMontoPrima
        WHEN "IdTipoInstrumento" = 'EMP' THEN :INGMontoPremio
        ELSE "INGPrecioAPlazo"  -- Esto mantiene el valor original si ninguna condición es verdadera
    END,
    "INGMontoATasaCompraActual" = :INGMontoATasaCompraActual,
    "INGMontoAPlazo" = :INGMontoAPlazo
WHERE "IdCartera" = :IdTablaOrigen;