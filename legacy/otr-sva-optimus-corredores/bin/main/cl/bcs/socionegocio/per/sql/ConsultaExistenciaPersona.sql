SELECT CP."IdPersona", SOC."IdSocioDeNegocio",COALESCE(SOC."IdCliente",0) 
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" CP 
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON SOC."IdPersona" = CP."IdPersona" 
WHERE CP."DocumentoIdentidad" = :DocumentoIdentidad 
AND CP."TipoDocumentoIdentidad" = :TipoDocumentoIdentidad