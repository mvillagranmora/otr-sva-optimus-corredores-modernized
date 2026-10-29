SELECT
	DocumentoIdentidad."DocumentoIdentidad"														AS "DocumentoIdentidad"
   ,Persona."Nombres" || ' ' || Persona."ApellidoPaterno" || ' ' || Persona."ApellidoMaterno"	AS "Nombres"
FROM
	"BSCL.SocioNegocio.BD::PER.Persona" AS Persona
INNER JOIN
	"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
ON   DocumentoIdentidad."IdDocumentoIdentidad" = Persona."IdDocumentoIdentidad"
WHERE Persona."IdPersona" = :IdPersona;