SELECT top 1 COALESCE("CobraPrimaAlInicio", 'S') AS "CobraPrimaAlInicio", "IdTipoPrestamo", "IdBovedaDefecto"
FROM "BSCL.Custodia.BD::PRT.TipoPrestamo" 
WHERE "IdInstrumentoPrestamo" = :p_IdInstrumentoPrestamo;