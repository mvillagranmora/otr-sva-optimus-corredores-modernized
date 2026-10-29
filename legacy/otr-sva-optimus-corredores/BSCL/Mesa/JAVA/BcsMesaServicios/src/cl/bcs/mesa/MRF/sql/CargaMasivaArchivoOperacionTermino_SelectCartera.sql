SELECT 
"FolioCartera"
,"Nemotecnico"
,"IdClasificacionTipoInstrumento"
,"CorteMinimo"
,"FechaVencimientoInstrumento" AS "FechaVencimiento"
,"IdMonedaInstrumento" AS "IdMoneda"
,"IdTipoReajuste"
,"IdMonedaEmision"
,"IdPortafolio"
FROM "BSCL.Custodia.BD::CUS.Cartera"
WHERE "FolioCartera" IN (:SecuenciaCartera) AND "IdEstado" = 'VIG'