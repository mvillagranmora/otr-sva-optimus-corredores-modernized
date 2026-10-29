SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoEnrolamientoCliente"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoEnrolamientoCliente" AS
SELECT
	CLIENTE."IdCliente"
	,CLIENTE."TipoEnrolamiento"
	,DOCUMENTO_IDENTIDAD."DocumentoIdentidad"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA ON CLIENTE."IdPersona" = PERSONA."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD ON DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad" = PERSONA."IdDocumentoIdentidad";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoEnrolamientoCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
