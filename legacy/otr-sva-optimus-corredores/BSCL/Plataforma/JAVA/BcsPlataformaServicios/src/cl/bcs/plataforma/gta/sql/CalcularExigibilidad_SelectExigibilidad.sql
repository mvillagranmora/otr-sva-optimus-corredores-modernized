SELECT 
 "IdBolsa"
,"IdTipoInstrumento"
,"IdTipoInstrumentoSubyacente"
,"ClasificacionRiesgo"
,CASE WHEN "IdTipoInstrumento" = 'DER' THEN '' ELSE "IdEntidadClasificadoraRiesgo" END  AS  "IdEntidadClasificadoraRiesgo"
,"IdTipoClasificacionRiesgo"
,"IdInstrumento"
,"ExigibilidadEntidad"
,"ExigibilidadCorredora"
FROM "BSCL.Plataforma.BD::GTA.ExigibilidadGarantia"
WHERE "IdInstrumento" IN (:IdInstrumento);
