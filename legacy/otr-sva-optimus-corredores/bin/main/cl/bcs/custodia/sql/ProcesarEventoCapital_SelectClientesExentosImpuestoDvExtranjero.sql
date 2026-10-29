SELECT "CLI"."IdCliente", CASE WHEN PER."Tipo" = 'J' AND TO_NVARCHAR("IdPaisResidencia")=:IdPaisEmisor THEN 'N'	ELSE 'S' END AS "AplicaImpuesto"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON (CLI."IdPersona"=PER."IdPersona" AND CLI."IdCliente" IN (:ids));