SELECT
	DocumentoIdentidad."DocumentoIdentidad"
   ,IFNULL(Persona."IdPersona",0)	AS "IdPersona"
   ,IFNULL(Cliente."IdCliente",0)	AS "IdCliente"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona ON Persona."IdDocumentoIdentidad" = DocumentoIdentidad."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente ON Cliente."IdPersona" = Persona."IdPersona"
WHERE
	DocumentoIdentidad."DocumentoIdentidad" IN(:p_DocumentoIdentidad);