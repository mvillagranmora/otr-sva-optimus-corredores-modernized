SELECT 
    COALESCE("DocumentoIdentidad", '') AS "DocumentoIdentidad",
    COALESCE("NombreCompleto", '') AS "NombreCompleto"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"
WHERE "IdPersona" = :IdPersona
