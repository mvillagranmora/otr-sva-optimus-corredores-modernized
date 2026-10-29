-- Actualiza los campos de Cliente
UPDATE  CLI 
SET     CLI."IdSegmentoCliente" = COALESCE(:IdSegmentoCliente,CLI."IdSegmentoCliente"),
		CLI."IdCategoriaInversionista" = COALESCE(:IdCategoriaInversionista,CLI."IdCategoriaInversionista"),
		CLI."IdPersonaEjecutivo" = COALESCE(:IdPersonaEjecutivo,CLI."IdPersonaEjecutivo"),
		CLI."IdTipoBloqueo" = COALESCE(:IdTipoBloqueo,CLI."IdTipoBloqueo"),
		CLI."FechaBloqueo" = COALESCE(:FechaBloqueo,CLI."FechaBloqueo"),
		CLI."ComentarioBloqueo" = COALESCE(:ComentarioBloqueo,CLI."ComentarioBloqueo"),
		CLI."IdTipoCliente" = COALESCE(:IdTipoCliente,CLI."IdTipoCliente"),
		CLI."CodigoAdicionalHolding" = COALESCE(:CodigoAdicionalHolding,CLI."CodigoAdicionalHolding"),
		CLI."IdSucursalPreferencia" = COALESCE(:IdSucursalPreferencia,CLI."IdSucursalPreferencia"),
		CLI."IdPersonaBrokerExtranjero" = COALESCE(:IdPersonaBrokerExtranjero,CLI."IdPersonaBrokerExtranjero"),
		CLI."IdClaseCliente" = COALESCE(:IdClaseCliente,CLI."IdClaseCliente"),
		CLI."EsClienteFinal" = COALESCE(:EsClienteFinal,CLI."EsClienteFinal"),
        	CLI."TipoEnrolamiento" = COALESCE(:TipoEnrolamiento,CLI."TipoEnrolamiento"),
		CLI."FechaModificacion" = CURRENT_UTCTIMESTAMP,
		CLI."CodigoEjecutivoExterno" = COALESCE(:CodigoEjecutivoExterno,CLI."CodigoEjecutivoExterno")
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
WHERE "IdCliente" = :IdCliente;