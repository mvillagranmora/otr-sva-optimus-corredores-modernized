SELECT	"IdInstrumentoRelacionado", 
		"NemoRelacionado", 
		"IdTipoInstrumentoRelacionado", 
		"IdClasificacionTipoInstrumentoRelacionado" 
FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarRelacionInstrumento"
WHERE 	"IdTipoRelacionInstrumento" 		= 'OPSUAC' 							AND 
		"IdInstrumento"						= :IdInstrumento					AND
		"IdEstado"							= 'VIG';