SELECT 
'S' AS "EntidadExiste"
, "IdEnrolamiento" AS "IdEntidad"
FROM "BSCL.SocioNegocio.BD::SOC.Enrolamiento" 
WHERE "DocumentoIdentidadAdicional" = :DocumentoIdentidad AND "IdTipoDocumentoIdentidadAdicional" = :IdTipoDocumentoIdentidad;