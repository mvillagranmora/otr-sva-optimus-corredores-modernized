-- Actualizar Persona asociada al Cliente
-- Cambia Estado de Persona solo si esta pendiente ('P' o 'N' para confirmar, 'P' para rechazar)
UPDATE PER
SET "EsEnrolado" = :EsEnrolado
FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
WHERE PER."IdPersona" IN (
	SELECT CLI."IdPersona"
	FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
	WHERE CLI."IdCliente" = :IdCliente
	AND PER."EsEnrolado" IN ('P', 'N')
)
