SELECT 
	"IdCorredora", 
	"IdPersona", 
	"CodigoInscripcionRegulador", 
	"IdEstado", 
	"Borrado", 
	"NombreCorto"
FROM "BSCL.Plataforma.BD::INS.Corredora"
WHERE "CodigoInscripcionRegulador" IN (:CodigoInscripcionRegulador) AND "IdPersona" IS NULL