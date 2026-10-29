SELECT COUNT(1) AS "Contador"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad"
WHERE "DocumentoIdentidad" = :DocumentoIdentidadAdicional
AND "IdTipoDocumentoIdentidad" = :IdTipoDocumentoIdentidadAdicional
AND "IdEmpresa" = :IdEmpresa
