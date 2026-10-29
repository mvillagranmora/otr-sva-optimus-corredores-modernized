-- Obtiene datos del cliente a modificar
SELECT		
		CLI."IdCliente",
		CLI."IdEstado",
		CLI."IdSegmentoCliente",
		CLI."IdCategoriaInversionista",
		CLI."IdPersonaEjecutivo",
		CLI."IdTipoBloqueo",
		CLI."FechaBloqueo",
		CLI."ComentarioBloqueo",
		CLI."IdTipoCliente",
		CLI."CodigoAdicionalHolding",
		CLI."IdSucursalPreferencia",
		CLI."IdPersonaBrokerExtranjero",
		CLI."IdClaseCliente",
		CLI."EsClienteFinal",
		CLI."CodigoEjecutivoExterno",
		CLI."TipoEnrolamiento",
		CASE "PER"."Tipo" 
			WHEN 'N' THEN "PER"."Nombres" ||' '|| "PER"."ApellidoPaterno" || ' ' ||"PER"."ApellidoMaterno" 
			ELSE "PER"."RazonSocial"
		END  as "NombreCompleto",
		IDENT."DocumentoIdentidad" AS "DocumentoIdentidad"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PER ON (PER."IdPersona" = CLI."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
WHERE  CLI."IdCliente" = :IdCliente;