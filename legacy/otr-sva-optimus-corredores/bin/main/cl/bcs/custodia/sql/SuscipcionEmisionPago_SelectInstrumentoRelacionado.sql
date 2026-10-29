SELECT	"IdInstrumento", 
		"IdInstrumentoRelacionado", 
		"NemoRelacionado"
FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarRelacionInstrumento"
WHERE	"IdInstrumento" 	IN (:p_IdInstrumento)
	AND "IdEstado"			= 'VIG';