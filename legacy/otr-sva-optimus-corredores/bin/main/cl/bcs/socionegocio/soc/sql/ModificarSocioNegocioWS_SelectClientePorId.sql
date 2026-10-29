SELECT 
Cliente."IdCliente" AS "IdCliente",
Persona."IdPersona"  AS "IdPersona"
FROM "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente ON Cliente."IdPersona" = Persona."IdPersona"
WHERE "IdCliente" = :IdCliente;