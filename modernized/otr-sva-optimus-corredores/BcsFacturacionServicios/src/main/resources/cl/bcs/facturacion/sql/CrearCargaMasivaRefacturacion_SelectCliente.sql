
SELECT
	COALESCE("CLI"."IdCliente",0) AS "IdCliente",
	"CLI"."IdEstado" AS "IdEstadoCliente",
	"PER"."IdEstado" AS "IdEstadoPersona",
	"IDENT"."DocumentoIdentidad",
	CASE "PER"."Tipo" 
		WHEN 'N' THEN "PER"."Nombres" ||' '|| "PER"."ApellidoPaterno" || ' ' ||"PER"."ApellidoMaterno"
		ELSE "PER"."RazonSocial"
	END  as "NombreCompleto",

	COALESCE("PORT"."IdPortafolio", 0) AS "IdPortafolio", 
	"PORT"."CodigoPortafolio",
	"PORT"."IdEstado" AS "IdEstadoPortafolio" 
	
FROM       "BSCL.SocioNegocio.BD::CLI.Cliente"  AS "CLI"
LEFT JOIN  "BSCL.Portafolio.BD::POR.Portafolio" AS "PORT" ON ("CLI"."IdCliente" = "PORT"."IdCliente")
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"  AS "PER" ON ( "PER"."IdPersona" = "CLI"."IdPersona" )
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "IDENT" ON "PER"."IdDocumentoIdentidad" = "IDENT"."IdDocumentoIdentidad"
WHERE "IDENT"."DocumentoIdentidad" IN (:DocumentoIdentidad);