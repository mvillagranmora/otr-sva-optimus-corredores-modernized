SELECT COUNT(1) AS "Contador"  
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" CDIP 
LEFT JOIN "::VISTA::" V ON CDIP."IdPersona" = V."IdPersona" 
WHERE CDIP."DocumentoIdentidad" = :DocumentoIdentidad 
AND CDIP."IdTipoDocumentoIdentidad" = :TipoDocumentoIdentidad 
AND CDIP."TipoPersona" = (CASE WHEN :CodigoPais = 'PE' AND :TipoPersona <> 'J' THEN :TipoPersona ELSE CDIP."TipoPersona" END)
AND V."IdPersona" IS NOT NULL;