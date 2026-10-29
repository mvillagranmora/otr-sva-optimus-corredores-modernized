SELECT
	CASE "PER"."Tipo" 
		WHEN 'N' THEN "PER"."Nombres" ||' '|| "PER"."ApellidoPaterno" || ' ' ||"PER"."ApellidoMaterno"
		ELSE "PER"."RazonSocial"
	END  as "NombreCompleto",
	"IDENT"."DocumentoIdentidad",		
	"PORT"."IdPortafolio", 
	"PORT"."CodigoPortafolio"
FROM       "BSCL.Portafolio.BD::POR.Portafolio" AS "PORT"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"  AS "CLI" ON ( "CLI"."IdCliente" = "PORT"."IdCliente" )
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"  AS "PER" ON ( "PER"."IdPersona" = "CLI"."IdPersona" )
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "IDENT" ON "PER"."IdDocumentoIdentidad" = "IDENT"."IdDocumentoIdentidad"
WHERE "IDENT"."DocumentoIdentidad" IN (:DocumentoIdentidad);