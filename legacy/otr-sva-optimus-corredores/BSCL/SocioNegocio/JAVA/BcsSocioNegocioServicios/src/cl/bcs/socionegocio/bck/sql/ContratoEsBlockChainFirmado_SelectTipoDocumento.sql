SELECT 	"IdClase",
		"TipoDocumentoNormativa"
FROM "BSCL.Plataforma.BD::DOC.TipoDocumento"
WHERE "IdTipoDocumento" = :p_IdTipoDocumento AND "IdClase" IN (2,3) /*tipos de contrato BlockChain*/ 
