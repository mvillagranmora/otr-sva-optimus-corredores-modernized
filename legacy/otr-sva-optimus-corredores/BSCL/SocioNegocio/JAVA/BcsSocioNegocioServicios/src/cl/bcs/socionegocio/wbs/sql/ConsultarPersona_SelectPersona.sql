SELECT
PER."IdPersona",
PER."Tipo",
CLI."TipoEnrolamiento",
CLI."IdCliente"
FROM "BSCL.SocioNegocio.BD::PER.Persona" AS PER
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = PER."IdPersona"
WHERE PER."IdPersona" IN (:IdPersonas);