SELECT 
  instrumento."IdInstrumento"
  ,instrumento."IdTipoInstrumento"  AS  "IdTipoInstrumentoInstrumentoAsociado"
  ,instrumento."Nemotecnico"  		AS  "NemoTecnicoInstrumentoAsociado"
  ,instrumento."IdMoneda"  			AS  "IdMonedaInstrumentoAsociado"
  ,rig."IdEntidadClasificadoraRiesgo" AS  "IdEntidadClasificadoraRiesgo"
  ,rig."ValorClasificacionRiesgo" 	  AS  "ClasificacionRiesgo"
FROM "BSCL.Plataforma.BD::INS.Instrumento" AS  instrumento
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" tipcl ON instrumento."IdTipoInstrumento" = tipcl."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" rig ON instrumento."IdInstrumento" = rig."IdInstrumento" AND rig."IdEntidadClasificadoraRiesgo" = tipcl."IdEntidadClasificadoraRiesgo"
WHERE instrumento."IdInstrumento" IN (:IdInstrumento);
