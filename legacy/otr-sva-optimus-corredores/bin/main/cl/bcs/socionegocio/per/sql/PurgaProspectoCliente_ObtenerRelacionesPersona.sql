SELECT DISTINCT DI."DocumentoIdentidad" AS "RutTitular"
FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" REL
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER
    ON PER."IdPersona" IN (REL."IdPersonaOrigen", REL."IdPersonaDestino")
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI
    ON PER."IdDocumentoIdentidad" = DI."IdDocumentoIdentidad"
WHERE DI."DocumentoIdentidad" IN (:DocumentosIdentidad) AND REL."IdEstado"='ACT';
