SELECT PER."IdPersona" AS "IdPersona"
     , CASE
           WHEN PER."Tipo" = 'J' THEN PER."RazonSocial"
           ELSE PER."Nombres" || ' ' || PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
    END                AS "NombreCompleto"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
     LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON (PER."IdPersona" = CLI."IdPersonaEjecutivo")
WHERE CLI."IdCliente" = :IdCliente;