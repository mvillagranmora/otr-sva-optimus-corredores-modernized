SELECT  
	SUM(ConsultarCarteraSubyacente."INGMontoATasaSubyacenteActual") AS "Monto"
	,ConsultarCarteraSubyacente."IdInstrumento" 				 AS  "IdInstrumento"
	,rig."IdEntidadClasificadoraRiesgo" 						 AS  "IdEntidadClasificadoraRiesgo"
	,rig."ValorClasificacionRiesgo" 							 AS  "ClasificacionRiesgo"
	,instrumento."IdTipoInstrumento"							 AS  "IdTipoInstrumento"
	,instrumento."Nemotecnico" 									 AS  "Nemotecnico"
	,instrumento."IdMoneda"  									 AS  "IdMoneda"
	,''                                                          AS  "TipoOpcion"
	,tipooper."Accion"    	                                     AS  "AccionTipoOperacion"
	,'CUSTODIA' 												 AS  "Origen"
FROM  "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos" 	 AS  ConsultarCarteraSaldos 
INNER  JOIN  "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica"  AS  ConsultarCarteraSubyacente ON  (ConsultarCarteraSubyacente."FolioCarteraRelacionada" = ConsultarCarteraSaldos."FolioCartera"	AND  ConsultarCarteraSubyacente."IdEstado" = 'VIG')
INNER  JOIN  "BSCL.Plataforma.BD::INS.Instrumento" AS  instrumento ON  instrumento."IdInstrumento" = ConsultarCarteraSubyacente."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" tipcl ON instrumento."IdTipoInstrumento" = tipcl."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" rig ON instrumento."IdInstrumento" = rig."IdInstrumento" AND rig."IdEntidadClasificadoraRiesgo" = tipcl."IdEntidadClasificadoraRiesgo"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipooper ON tipooper."IdTipoOperacion" = ConsultarCarteraSaldos."IdTipoOperacion"
WHERE  ConsultarCarteraSaldos."IdBolsa" = :p_IdBolsa
 AND  ConsultarCarteraSaldos."IdPortafolio" = :p_IdPortafolio
 AND  ConsultarCarteraSaldos."IdTipoInstrumento" = :p_IdTipoInstrumento
 AND  ConsultarCarteraSaldos."IdInstrumento"  = :p_IdInstrumento 
GROUP BY 
ConsultarCarteraSubyacente."IdInstrumento"
,rig."IdEntidadClasificadoraRiesgo"
,rig."ValorClasificacionRiesgo"
,instrumento."IdTipoInstrumento"
,instrumento."Nemotecnico"
,instrumento."IdMoneda"
,tipooper."Accion"; 