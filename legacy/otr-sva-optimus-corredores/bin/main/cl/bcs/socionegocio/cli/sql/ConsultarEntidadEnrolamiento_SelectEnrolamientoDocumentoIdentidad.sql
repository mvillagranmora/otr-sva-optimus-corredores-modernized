SELECT 
'S' AS "EntidadExiste"
, "IdEnrolamiento" AS "IdEntidad"
FROM "BSCL.SocioNegocio.BD::SOC.Enrolamiento" 
WHERE "DocumentoIdentidad" = :DocumentoIdentidad AND "IdTipoDocumentoIdentidad" = :IdTipoDocumentoIdentidad;