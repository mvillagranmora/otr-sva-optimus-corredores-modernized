SELECT "IdPersona"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarDocumentoIdentidadPersona"
WHERE upper("DocumentoIdentidad")  = upper(:p_DocumentoIdentidad)
