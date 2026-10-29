SELECT  "IdInstrumentoRelacionado"
FROM "BSCL.Plataforma.BD::INS.RelacionInstrumento"
WHERE  
	"IdTipoRelacionInstrumento" 		= 'OPSUAC' 							and 
	"IdInstrumento"						= :IdInstrumento					and
	"IdEstado"							= 'VIG';