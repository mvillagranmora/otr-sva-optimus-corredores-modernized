SELECT
"IdEnrolamiento"
FROM "BSCL.SocioNegocio.BD::SOC.Enrolamiento" WHERE "DocumentoIdentidad" = :DocumentoIdentidad AND "Tipo" = :IdTipoPersona;