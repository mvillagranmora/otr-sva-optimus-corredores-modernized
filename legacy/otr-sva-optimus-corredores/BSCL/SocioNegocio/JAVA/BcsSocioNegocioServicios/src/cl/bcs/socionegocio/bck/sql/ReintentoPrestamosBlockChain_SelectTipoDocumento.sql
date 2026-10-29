SELECT "IdClase"
	   ,"TipoDocumentoNormativa"
FROM "BSCL.Plataforma.BD::DOC.TipoDocumento"
WHERE "IdEmpresa" = :p_IdEmpresa 
AND "IdTipoDocumento" IN ( :p_TipoDocumento )
AND "IdClase" IN (2,3) /*tipos de contrato BlockChain*/ 


