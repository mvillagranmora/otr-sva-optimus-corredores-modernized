SELECT
	DocumentoIdentidad."DocumentoIdentidad"
   ,Persona."IdPersona"
   ,Cliente."IdCliente"
   ,Persona."Nombres"
   ,CASE Persona."Tipo" 
		WHEN 'N' THEN Persona."Nombres" ||' '|| Persona."ApellidoPaterno" || ' ' || Persona."ApellidoMaterno" 
		ELSE Persona."RazonSocial"
		END  AS "NombreCompleto"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona ON Persona."IdDocumentoIdentidad" = DocumentoIdentidad."IdDocumentoIdentidad"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente ON Cliente."IdPersona" = Persona."IdPersona"
WHERE Cliente."IdCliente" in (:p_IdCliente);