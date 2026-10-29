SELECT 
PER."IdPersona"
,SOC."IdSocioDeNegocio"
,DOC."DocumentoIdentidad"
,DOC."IdTipoDocumentoIdentidad" AS "TipoDocumentoIdentidad"
,PER."Tipo"
FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON SOC."IdPersona" = PER."IdPersona" 
WHERE DOC."DocumentoIdentidad" IN (:DocumentoIdentidad) 
AND DOC."IdTipoDocumentoIdentidad" IN (:TDI) 
AND SOC."IdSocioDeNegocio" IS NOT NULL