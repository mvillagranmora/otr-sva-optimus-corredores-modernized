SELECT VCR."IdPersonaDestino"      AS "IdPersona"
     , VCR."NombreCompletoDestino" AS "NombreCompleto"
FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" VCR
     LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON (PER."IdPersona" = :IdPersona)
WHERE VCR."IdPersonaOrigen" = :IdPersona
  AND VCR."UsaFirmaDocumento" = 'S'
  AND VCR."IdEstado" = 'ACT'
UNION ALL
SELECT PER."IdPersona" AS "IdPersona"
     , CASE
           WHEN PER."Tipo" = 'J' THEN PER."RazonSocial"
           ELSE PER."Nombres" || ' ' || PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
    END                AS "NombreCompleto"
FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
WHERE PER."IdPersona" = :IdPersona;