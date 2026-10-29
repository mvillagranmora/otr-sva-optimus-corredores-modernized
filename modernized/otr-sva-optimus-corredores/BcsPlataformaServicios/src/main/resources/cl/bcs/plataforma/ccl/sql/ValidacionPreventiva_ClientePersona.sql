SELECT CLI."IdCliente" AS "Valor5" 
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER 
ON CLI."IdPersona" = PER."IdPersona"
WHERE PER."IdPersona" IS NULL;
