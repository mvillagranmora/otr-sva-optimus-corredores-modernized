SELECT 
"IdInstrumento"
,COALESCE("PrecioMedio",TO_DECIMAL(0,26,8)) as "PrecioMedio"
FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" 
WHERE "IdInstrumento" IN (:IdInstrumento)
AND "Fecha" = :Fecha AND "IdBolsa" = 'BCS'