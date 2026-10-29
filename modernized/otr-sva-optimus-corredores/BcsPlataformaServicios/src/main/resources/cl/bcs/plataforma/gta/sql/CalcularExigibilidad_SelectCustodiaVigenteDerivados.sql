SELECT  
	SUM(ConsultarCarteraSaldos."INGMontoATasaMercadoActual")     AS  "Monto"
	,ConsultarCarteraSaldos."IdInstrumento" 				     AS  "IdInstrumento"
	,rig."IdEntidadClasificadoraRiesgo" 						 AS  "IdEntidadClasificadoraRiesgo"
	,rig."ValorClasificacionRiesgo" 							 AS  "ClasificacionRiesgo"
	,instrumento."IdTipoInstrumento"							 AS  "IdTipoInstrumento"
	,instrumento."Nemotecnico" 									 AS  "Nemotecnico"
	,instrumento."IdMoneda"  									 AS  "IdMoneda"
	,COALESCE(ConsultarCarteraSaldos."TipoOpcion",'')            AS  "TipoOpcion"
	,tipooper."Accion"    	                                     AS  "AccionOperacion"
	,'CUSTODIA' 												 AS  "Origen"
FROM  "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos" 	 AS  ConsultarCarteraSaldos 
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS  instrumento ON  instrumento."IdInstrumento" = ConsultarCarteraSaldos."IdInstrumento"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoinstrumento ON tipoinstrumento."IdTipoInstrumento" = instrumento."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" tipcl ON instrumento."IdTipoInstrumento" = tipcl."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" rig ON instrumento."IdInstrumento" = rig."IdInstrumento" AND rig."IdEntidadClasificadoraRiesgo" = tipcl."IdEntidadClasificadoraRiesgo"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipooper ON tipooper."IdTipoOperacion" = ConsultarCarteraSaldos."IdTipoOperacion"
WHERE  ConsultarCarteraSaldos."IdBolsa" = :p_IdBolsa
  AND  ConsultarCarteraSaldos."IdPortafolio" = :p_IdPortafolio
  AND tipoinstrumento."IdClasificacionTipoInstrumento" = 'DV' 
  AND tipoinstrumento."Codigo" = 'DER'
GROUP BY 
ConsultarCarteraSaldos."IdInstrumento"
,rig."IdEntidadClasificadoraRiesgo"
,rig."ValorClasificacionRiesgo"
,instrumento."IdTipoInstrumento"
,instrumento."Nemotecnico"
,instrumento."IdMoneda"
,ConsultarCarteraSaldos."TipoOpcion"
,tipooper."Accion";