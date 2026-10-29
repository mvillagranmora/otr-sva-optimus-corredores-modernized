SELECT PER."DocumentoIdentidad" AS "RutTitular"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON CLI."IdPersona" = PER."IdPersona"
WHERE PER."DocumentoIdentidad" IN (:DocumentosIdentidad)
AND (PER."IdTipoBloqueo"='BMUERT' OR CLI."IdTipoBloqueo"='BMUERT');