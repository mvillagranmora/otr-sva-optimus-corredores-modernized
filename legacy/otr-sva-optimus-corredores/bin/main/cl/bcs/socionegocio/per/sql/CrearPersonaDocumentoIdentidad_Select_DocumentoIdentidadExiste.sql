SELECT COUNT(1) AS "Count"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"
WHERE "DocumentoIdentidad" = :DocumentoIdentidad AND "TipoDocumentoIdentidad" = :IdTipoDocumentoIdentidad
