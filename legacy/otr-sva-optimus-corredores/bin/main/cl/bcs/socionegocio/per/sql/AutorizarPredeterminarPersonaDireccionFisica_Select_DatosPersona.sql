SELECT  
    CASE WHEN LENGTH(NOM."NombreCompleto") > 100 
        THEN LEFT((NOM."NombreCompleto"),100) 
        ELSE NOM."NombreCompleto" 
    END AS "Nombres",
    DI."DocumentoIdentidad" AS "DocumentoIdentidad",
    DI."IdTipoDocumentoIdentidad" AS "IdTipoDocumentoIdentidad"
FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI 
    ON (PER."IdDocumentoIdentidad" = DI."IdDocumentoIdentidad")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" NOM 
    ON (PER."IdPersona" = NOM."IdPersona")
WHERE PER."IdPersona" = :IdPersona
