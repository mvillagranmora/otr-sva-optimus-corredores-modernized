SELECT FPR."PaisResidencia", FPR."TIN", FPR."CausaNoTin" 
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaFatcaPaisResidencia" FPR
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER  ON PER."IdPersona" = FPR."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
WHERE DOC."DocumentoIdentidad" = :DocumentoIdentidad;